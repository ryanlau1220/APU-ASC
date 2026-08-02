package com.apu.asc.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

  @Mock private UserApi userApi;

  @Test
  void resolvesTheInternalUserIdFromTheVerifiedPrincipalName() {
    UserDto user =
        new UserDto(
            "USR-CUSTOMER",
            "KC-CUSTOMER",
            "customer",
            "customer@example.com",
            "Customer",
            "CUSTOMER",
            "ACTIVE",
            null,
            null);
    TestingAuthenticationToken authentication =
        new TestingAuthenticationToken(
            "KC-CUSTOMER", "n/a", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    authentication.setAuthenticated(true);
    when(userApi.findByKeycloakId("KC-CUSTOMER")).thenReturn(Optional.of(user));

    var currentUser = new CurrentUserService(userApi).requireCurrentUser(authentication);

    assertThat(currentUser.id()).isEqualTo("USR-CUSTOMER");
    assertThat(currentUser.roles()).containsExactly("CUSTOMER");
    verify(userApi).findByKeycloakId("KC-CUSTOMER");
  }
}
