package com.apu.asc.user.internal;

import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & Registration", description = "Current user session hydration")
class MeController {

  private final UserApi userApi;

  @GetMapping("/me")
  @Operation(
      summary = "Get current authenticated user profile",
      description = "Hydrates current session user details, roles, and PostgreSQL ID")
  public ResponseEntity<?> getCurrentUser(Authentication authentication) {
    if (authentication == null
        || !authentication.isAuthenticated()
        || "anonymousUser".equals(authentication.getPrincipal())) {
      return ResponseEntity.ok(Map.of("authenticated", false, "roles", List.of()));
    }

    Object principal = authentication.getPrincipal();
    String username = null;
    String email = null;
    String sub = null;

    if (principal instanceof OidcUser oidcUser) {
      username = oidcUser.getPreferredUsername();
      email = oidcUser.getEmail();
      sub = oidcUser.getSubject();
    } else if (principal instanceof Jwt jwt) {
      username = jwt.getClaimAsString("preferred_username");
      email = jwt.getClaimAsString("email");
      sub = jwt.getSubject();
    } else {
      username = authentication.getName();
    }

    List<String> roles =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(a -> a.startsWith("ROLE_"))
            .map(a -> a.substring(5))
            .toList();

    UserDto userDto = null;
    if (username != null) {
      try {
        userDto = userApi.findByUsername(username).orElse(null);
      } catch (Exception ignored) {
      }
    }

    return ResponseEntity.ok(
        Map.of(
            "authenticated",
            true,
            "id",
            userDto != null ? userDto.id() : sub,
            "keycloakId",
            sub != null ? sub : "",
            "username",
            username != null ? username : "",
            "email",
            email != null ? email : "",
            "fullName",
            userDto != null ? userDto.fullName() : username,
            "roles",
            roles));
  }
}
