package com.apu.asc.common.security;

import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Central role and ownership rules for HTTP resources. Resource modules supply the resource's owner
 * and, where applicable, assigned technician; this policy never trusts a client-supplied owner
 * identifier.
 */
@Component
public class AccessPolicy {

  private static final Set<String> OPERATIONAL_ROLES = Set.of("STAFF", "MANAGER", "SYSTEM_ADMIN");
  private static final Set<String> MANAGER_ROLES = Set.of("MANAGER", "SYSTEM_ADMIN");

  public boolean isOperationalUser(AuthenticatedUser user) {
    return user.roles().stream().anyMatch(OPERATIONAL_ROLES::contains);
  }

  public boolean isManager(AuthenticatedUser user) {
    return user.roles().stream().anyMatch(MANAGER_ROLES::contains);
  }

  public boolean isTechnician(AuthenticatedUser user) {
    return user.hasRole("TECHNICIAN");
  }

  public void requireSelfOrOperational(AuthenticatedUser user, String subjectUserId) {
    if (isOperationalUser(user) || user.id().equals(subjectUserId)) {
      return;
    }
    deny();
  }

  public void requireAppointmentRead(
      AuthenticatedUser user, String customerId, String technicianId) {
    if (isOperationalUser(user)
        || user.id().equals(customerId)
        || (isTechnician(user) && user.id().equals(technicianId))) {
      return;
    }
    deny();
  }

  public void requireAssignedTechnicianOrOperational(AuthenticatedUser user, String technicianId) {
    if (isOperationalUser(user) || (isTechnician(user) && user.id().equals(technicianId))) {
      return;
    }
    deny();
  }

  public void requireFeedbackRead(AuthenticatedUser user, String customerId, String technicianId) {
    if (isOperationalUser(user)
        || user.id().equals(customerId)
        || (isTechnician(user) && user.id().equals(technicianId))) {
      return;
    }
    deny();
  }

  private void deny() {
    throw new AccessDeniedException("You do not have permission to access this resource.");
  }
}
