package com.apu.asc.appointment.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.vehicle.VehicleApi;
import com.apu.asc.vehicle.VehicleDto;
import java.time.LocalDate;
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
class AppointmentControllerAuthorizationTest {

  private static final Authentication AUTHENTICATION =
      new TestingAuthenticationToken("customer-a", "n/a");

  @Mock private AppointmentApi appointmentApi;
  @Mock private VehicleApi vehicleApi;
  @Mock private CurrentUserService currentUserService;

  private AppointmentController controller;

  @BeforeEach
  void setUp() {
    controller =
        new AppointmentController(
            appointmentApi, vehicleApi, currentUserService, new AccessPolicy());
  }

  @Test
  void customerCannotReadAnotherCustomersAppointment() {
    when(appointmentApi.getAppointmentById("APT-OTHER")).thenReturn(appointment("USR-OTHER"));
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));

    assertThatThrownBy(() -> controller.getAppointmentById("APT-OTHER", AUTHENTICATION))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void customerCannotCancelAnotherCustomersPendingAppointment() {
    when(appointmentApi.getAppointmentById("APT-OTHER")).thenReturn(appointment("USR-OTHER"));
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));

    assertThatThrownBy(() -> controller.cancelAppointment("APT-OTHER", AUTHENTICATION))
        .isInstanceOf(AccessDeniedException.class);
    verifyNoInteractions(vehicleApi);
  }

  @Test
  void customerCannotCancelAConfirmedAppointment() {
    when(appointmentApi.getAppointmentById("APT-OTHER"))
        .thenReturn(confirmedAppointment("USR-CUSTOMER"));
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));

    assertThatThrownBy(() -> controller.cancelAppointment("APT-OTHER", AUTHENTICATION))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("pending");
    verify(appointmentApi).getAppointmentById("APT-OTHER");
  }

  @Test
  void customerCannotRescheduleAConfirmedAppointment() {
    AppointmentDto appointment = confirmedAppointment("USR-CUSTOMER");
    when(appointmentApi.getAppointmentById("APT-OTHER")).thenReturn(appointment);
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-CUSTOMER", Set.of("CUSTOMER")));

    assertThatThrownBy(() -> controller.updateAppointment("APT-OTHER", appointment, AUTHENTICATION))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("pending");
    verifyNoInteractions(vehicleApi);
  }

  @Test
  void operationalBookingWithoutCustomerIdUsesTheSelectedVehiclesOwner() {
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-STAFF", Set.of("STAFF")));
    when(vehicleApi.getVehicleById("VEH-1")).thenReturn(vehicle("USR-CUSTOMER"));
    when(appointmentApi.createAppointment(any()))
        .thenAnswer(
            invocation -> {
              AppointmentDto request = invocation.getArgument(0);
              return new AppointmentDto(
                  "APT-NEW",
                  request.customerId(),
                  request.vehicleId(),
                  request.serviceId(),
                  request.technicianId(),
                  request.appointmentDate(),
                  request.timeSlot(),
                  request.status(),
                  request.notes(),
                  null,
                  null);
            });

    AppointmentDto request =
        new AppointmentDto(
            null,
            null,
            "VEH-1",
            "SVC-1",
            null,
            LocalDate.now().plusDays(1),
            "09:00-10:00",
            null,
            null,
            null,
            null);

    AppointmentDto created = controller.createAppointment(request, AUTHENTICATION).getBody();

    assertThat(created.customerId()).isEqualTo("USR-CUSTOMER");
    assertThat(created.status()).isEqualTo("PENDING");
  }

  @Test
  void operationalBookingCannotPairACustomerWithAnotherCustomersVehicle() {
    when(currentUserService.requireCurrentUser(AUTHENTICATION))
        .thenReturn(new AuthenticatedUser("USR-STAFF", Set.of("STAFF")));
    when(vehicleApi.getVehicleById("VEH-1")).thenReturn(vehicle("USR-CUSTOMER"));

    AppointmentDto request =
        new AppointmentDto(
            null,
            "USR-OTHER",
            "VEH-1",
            "SVC-1",
            null,
            LocalDate.now().plusDays(1),
            "09:00-10:00",
            null,
            null,
            null,
            null);

    assertThatThrownBy(() -> controller.createAppointment(request, AUTHENTICATION))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("does not belong");
    verifyNoInteractions(appointmentApi);
  }

  private AppointmentDto appointment(String customerId) {
    return new AppointmentDto(
        "APT-OTHER",
        customerId,
        "VEH-OTHER",
        "SVC-1",
        "USR-TECHNICIAN",
        LocalDate.now().plusDays(1),
        "09:00",
        "PENDING",
        null,
        null,
        null);
  }

  private AppointmentDto confirmedAppointment(String customerId) {
    AppointmentDto appointment = appointment(customerId);
    return new AppointmentDto(
        appointment.id(),
        appointment.customerId(),
        appointment.vehicleId(),
        appointment.serviceId(),
        appointment.technicianId(),
        appointment.appointmentDate(),
        appointment.timeSlot(),
        "CONFIRMED",
        appointment.notes(),
        appointment.createdAt(),
        appointment.updatedAt());
  }

  private VehicleDto vehicle(String customerId) {
    return new VehicleDto("VEH-1", customerId, "ABC1234", "Honda", "City", 2024, null);
  }
}
