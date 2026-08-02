package com.apu.asc.common.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class AccessPolicyTest {

  private final AccessPolicy accessPolicy = new AccessPolicy();
  private final AuthenticatedUser customer =
      new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER"));
  private final AuthenticatedUser technician =
      new AuthenticatedUser("USR-TECHNICIAN", Set.of("TECHNICIAN"));
  private final AuthenticatedUser staff = new AuthenticatedUser("USR-STAFF", Set.of("STAFF"));

  @Test
  void customerCannotAccessAnotherCustomersResource() {
    assertThatThrownBy(() -> accessPolicy.requireSelfOrOperational(customer, "USR-OTHER"))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void technicianCanReadOnlyAnAssignedAppointment() {
    assertThatCode(
            () -> accessPolicy.requireAppointmentRead(technician, "USR-CUSTOMER", "USR-TECHNICIAN"))
        .doesNotThrowAnyException();

    assertThatThrownBy(
            () ->
                accessPolicy.requireAppointmentRead(
                    technician, "USR-CUSTOMER", "USR-OTHER-TECHNICIAN"))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void operationalStaffCanAccessCustomerOwnedOperationalResources() {
    assertThatCode(() -> accessPolicy.requireSelfOrOperational(staff, "USR-CUSTOMER"))
        .doesNotThrowAnyException();
  }
}
