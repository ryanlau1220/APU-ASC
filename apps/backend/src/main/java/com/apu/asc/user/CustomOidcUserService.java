package com.apu.asc.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class CustomOidcUserService extends OidcUserService {

  private final UserApi userApi;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  @SuppressWarnings("unchecked")
  public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
    OidcUser oidcUser = super.loadUser(userRequest);

    String sub = oidcUser.getSubject();
    String username = oidcUser.getPreferredUsername();
    String email = oidcUser.getEmail();
    String fullName = oidcUser.getFullName();
    if (fullName == null || fullName.isBlank()) {
      fullName = oidcUser.getGivenName();
    }
    if (fullName == null || fullName.isBlank()) {
      fullName = username;
    }

    Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());
    String primaryRole = "CUSTOMER";

    Map<String, Object> realmAccess = null;
    if (oidcUser.getIdToken() != null
        && oidcUser.getIdToken().getClaim("realm_access") instanceof Map<?, ?> map) {
      realmAccess = (Map<String, Object>) map;
    } else if (oidcUser.getAttribute("realm_access") instanceof Map<?, ?> map) {
      realmAccess = (Map<String, Object>) map;
    } else if (oidcUser.getClaims().get("realm_access") instanceof Map<?, ?> map) {
      realmAccess = (Map<String, Object>) map;
    }

    if (realmAccess == null && userRequest.getAccessToken() != null) {
      try {
        String tokenVal = userRequest.getAccessToken().getTokenValue();
        String[] parts = tokenVal.split("\\.");
        if (parts.length >= 2) {
          String payloadJson =
              new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
          Map<String, Object> tokenClaims = objectMapper.readValue(payloadJson, Map.class);
          if (tokenClaims.get("realm_access") instanceof Map<?, ?> map) {
            realmAccess = (Map<String, Object>) map;
          }
        }
      } catch (Exception e) {
        log.debug("Could not parse access token claims: {}", e.getMessage());
      }
    }

    if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
      for (Object r : roles) {
        String roleStr = r.toString().toUpperCase();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr));

        if ("SYSTEM_ADMIN".equals(roleStr) || "MANAGER".equals(roleStr)) {
          authorities.add(new SimpleGrantedAuthority("ROLE_SYSTEM_ADMIN"));
          authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
          authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
          primaryRole = "MANAGER";
        } else if ("STAFF".equals(roleStr)) {
          authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
          if ("CUSTOMER".equals(primaryRole)) {
            primaryRole = "STAFF";
          }
        } else if ("TECHNICIAN".equals(roleStr)) {
          authorities.add(new SimpleGrantedAuthority("ROLE_TECHNICIAN"));
          if ("CUSTOMER".equals(primaryRole)) {
            primaryRole = "TECHNICIAN";
          }
        }
      }
    }

    try {
      UserDto syncedUser = userApi.syncJitUser(sub, username, email, fullName, primaryRole);
      log.info("JIT User Provisioning completed for user sub: {}, id: {}", sub, syncedUser.id());
    } catch (Exception e) {
      log.error("Failed to execute JIT user sync for sub {}: {}", sub, e.getMessage(), e);
      throw new OAuth2AuthenticationException(
          new OAuth2Error("jit_provisioning_failed"), "Unable to provision this account.");
    }

    String nameAttributeKey =
        userRequest
            .getClientRegistration()
            .getProviderDetails()
            .getUserInfoEndpoint()
            .getUserNameAttributeName();
    if (nameAttributeKey == null || nameAttributeKey.isBlank()) {
      nameAttributeKey = "sub";
    }

    return new DefaultOidcUser(
        authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), nameAttributeKey);
  }
}
