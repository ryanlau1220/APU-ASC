package com.apu.asc.user.internal;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.apu.asc.config.SecurityConfig;
import com.apu.asc.user.CustomOidcUserService;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MeController.class)
@Import(SecurityConfig.class)
class MeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserApi userApi;
  @MockBean private CustomOidcUserService customOidcUserService;

  @MockBean
  private org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
      clientRegistrationRepository;

  @Test
  @DisplayName("Should return 200 OK with authenticated=false for guest session hydration")
  void shouldReturnAuthenticatedFalseForUnauthenticatedRequest() throws Exception {
    mockMvc
        .perform(get("/api/v1/auth/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authenticated").value(false));
  }

  @Test
  @DisplayName("Should redirect a signed-out browser to the frontend login page")
  void shouldRedirectLogoutToFrontendLogin() throws Exception {
    mockMvc
        .perform(get("/logout"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("http://localhost:3000/login?loggedOut=true"));
  }

  @Test
  @DisplayName("Should return authenticated user profile for valid session")
  void shouldReturnUserProfileForAuthenticatedSession() throws Exception {
    UserDto mockDto =
        new UserDto(
            "USR-101",
            "sub-keycloak-admin",
            "admin",
            "admin@apu-asc.com",
            "System Admin",
            "SYSTEM_ADMIN",
            "ACTIVE",
            null,
            null,
            null);

    given(userApi.findByUsername(anyString())).willReturn(Optional.of(mockDto));

    mockMvc
        .perform(
            get("/api/v1/auth/me")
                .with(
                    oidcLogin()
                        .idToken(
                            token ->
                                token
                                    .claim("preferred_username", "admin")
                                    .claim("email", "admin@apu-asc.com")
                                    .claim("sub", "sub-keycloak-admin"))
                        .authorities(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authenticated").value(true))
        .andExpect(jsonPath("$.username").value("admin"))
        .andExpect(jsonPath("$.roles[0]").value("SYSTEM_ADMIN"));
  }
}
