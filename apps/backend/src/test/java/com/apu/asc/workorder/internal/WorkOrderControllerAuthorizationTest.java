package com.apu.asc.workorder.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.vehicle.VehicleApi;
import com.apu.asc.workorder.WorkOrderApi;
import com.apu.asc.workorder.WorkOrderDto;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class WorkOrderControllerAuthorizationTest {

  private static final Authentication AUTHENTICATION =
      new TestingAuthenticationToken("customer-a", "n/a");

  @Mock private WorkOrderApi workOrderApi;
  @Mock private AppointmentApi appointmentApi;
  @Mock private VehicleApi vehicleApi;
  @Mock private CurrentUserService currentUserService;

  private WorkOrderController controller;

  @BeforeEach
  void setUp() {
    controller =
        new WorkOrderController(
            workOrderApi, appointmentApi, vehicleApi, currentUserService, new AccessPolicy());
  }

  @Test
  void customerCannotReadAnotherCustomersWorkOrder() {
    when(workOrderApi.getWorkOrderById("WO-OTHER")).thenReturn(workOrder("USR-OTHER"));
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));

    assertThatThrownBy(() -> controller.getWorkOrderById("WO-OTHER", AUTHENTICATION))
        .isInstanceOf(AccessDeniedException.class);
  }

  private WorkOrderDto workOrder(String customerId) {
    return new WorkOrderDto(
        "WO-OTHER",
        "APT-OTHER",
        customerId,
        "VEH-OTHER",
        "SVC-1",
        "USR-TECH",
        "OPEN",
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }
}
