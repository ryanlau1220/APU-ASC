package com.apu.asc.common.security;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;

/** The authenticated application user, resolved from a verified Keycloak identity. */
@SuppressFBWarnings({"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public record AuthenticatedUser(String id, Set<String> roles) {

  public boolean hasRole(String role) {
    return roles.contains(role);
  }
}
