package com.apu.asc.common.security;

import java.util.Set;

/** The authenticated application user, resolved from a verified Keycloak identity. */
public record AuthenticatedUser(String id, Set<String> roles) {

  public boolean hasRole(String role) {
    return roles.contains(role);
  }
}
