package com.apu.asc.user;

import com.apu.asc.common.security.AuthenticatedUser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/** Resolves a verified Spring Security principal to the internal application user identifier. */
@Service
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class CurrentUserService {

  private final UserApi userApi;

  public CurrentUserService(UserApi userApi) {
    this.userApi = userApi;
  }

  public AuthenticatedUser requireCurrentUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new AccessDeniedException("Authentication is required.");
    }

    UserDto user = resolveUser(authentication).orElseThrow(this::unknownUser);
    return new AuthenticatedUser(user.id(), extractRoles(authentication));
  }

  private Optional<UserDto> resolveUser(Authentication authentication) {
    LinkedHashSet<String> subjects = new LinkedHashSet<>();
    LinkedHashSet<String> usernames = new LinkedHashSet<>();
    LinkedHashSet<String> emails = new LinkedHashSet<>();
    LinkedHashSet<String> ids = new LinkedHashSet<>();

    Object principal = authentication.getPrincipal();
    if (principal instanceof OidcUser oidcUser) {
      add(subjects, oidcUser.getSubject());
      add(usernames, oidcUser.getPreferredUsername());
      add(emails, oidcUser.getEmail());
    } else if (principal instanceof Jwt jwt) {
      add(subjects, jwt.getSubject());
      add(usernames, jwt.getClaimAsString("preferred_username"));
      add(emails, jwt.getClaimAsString("email"));
    }

    add(subjects, authentication.getName());
    add(ids, authentication.getName());

    for (String subject : subjects) {
      Optional<UserDto> user = userApi.findByKeycloakId(subject);
      if (user.isPresent()) {
        return user;
      }
    }
    for (String username : usernames) {
      Optional<UserDto> user = userApi.findByUsername(username);
      if (user.isPresent()) {
        return user;
      }
    }
    for (String email : emails) {
      Optional<UserDto> user = userApi.findByEmail(email);
      if (user.isPresent()) {
        return user;
      }
    }
    for (String id : ids) {
      Optional<UserDto> user = userApi.findById(id);
      if (user.isPresent()) {
        return user;
      }
    }
    return Optional.empty();
  }

  private Set<String> extractRoles(Authentication authentication) {
    return authentication.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority)
        .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
        .map(String::toUpperCase)
        .collect(java.util.stream.Collectors.toUnmodifiableSet());
  }

  private void add(Set<String> candidates, String value) {
    if (value != null && !value.isBlank()) {
      candidates.add(value);
    }
  }

  private AccessDeniedException unknownUser() {
    return new AccessDeniedException(
        "Authenticated account is not provisioned in this application.");
  }
}
