package com.apu.asc.user;

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
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOidcUserService extends OidcUserService {

  private final UserApi userApi;

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

    Map<String, Object> claims = oidcUser.getClaims();
    Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
    if (realmAccess != null && realmAccess.get("roles") instanceof List<?> roles) {
      for (Object r : roles) {
        String roleStr = r.toString();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleStr));
        if ("SYSTEM_ADMIN".equalsIgnoreCase(roleStr)
            || "WORKSHOP_MANAGER".equalsIgnoreCase(roleStr)
            || "TECHNICIAN".equalsIgnoreCase(roleStr)) {
          primaryRole = roleStr.toUpperCase();
        }
      }
    }

    try {
      UserDto syncedUser = userApi.syncJitUser(sub, username, email, fullName, primaryRole);
      log.info("JIT User Provisioning completed for user sub: {}, id: {}", sub, syncedUser.id());
    } catch (Exception e) {
      log.error("Failed to execute JIT user sync for sub {}: {}", sub, e.getMessage(), e);
    }

    return new DefaultOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo());
  }
}
