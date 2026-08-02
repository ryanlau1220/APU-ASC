package com.apu.asc.vehicle.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.vehicle.VehicleApi;
import com.apu.asc.vehicle.VehicleDto;
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
class VehicleControllerAuthorizationTest {

  private static final Authentication AUTHENTICATION =
      new TestingAuthenticationToken("customer-a", "n/a");

  @Mock private VehicleApi vehicleApi;
  @Mock private CurrentUserService currentUserService;

  private VehicleController controller;

  @BeforeEach
  void setUp() {
    controller = new VehicleController(vehicleApi, currentUserService, new AccessPolicy());
  }

  @Test
  void customerCannotReadAnotherCustomersVehicle() {
    when(vehicleApi.getVehicleById("VEH-OTHER")).thenReturn(vehicle("USR-OTHER"));
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));

    assertThatThrownBy(() -> controller.getVehicleById("VEH-OTHER", AUTHENTICATION))
        .isInstanceOf(AccessDeniedException.class);
  }

  private VehicleDto vehicle(String customerId) {
    return new VehicleDto("VEH-OTHER", customerId, "ABC1234", "Honda", "Civic", 2024, null);
  }
}
