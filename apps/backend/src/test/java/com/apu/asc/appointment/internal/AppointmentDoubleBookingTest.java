package com.apu.asc.appointment.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.apu.asc.appointment.AppointmentDto;
import com.apu.asc.scheduling.SchedulingApi;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AppointmentDoubleBookingTest {

  @Mock private AppointmentRepository appointmentRepository;
  @Mock private SchedulingApi schedulingApi;
  @Mock private ApplicationEventPublisher eventPublisher;

  private AppointmentServiceImpl appointmentService;

  @BeforeEach
  void setUp() {
    appointmentService =
        new AppointmentServiceImpl(appointmentRepository, schedulingApi, eventPublisher);
  }

  @Test
  @DisplayName("Should reject appointment if customer already has booking for slot")
  void shouldRejectCustomerDoubleBooking() {
    LocalDate today = LocalDate.now();
    Mockito.when(
            appointmentRepository.existsByCustomerIdAndAppointmentDateAndTimeSlotAndStatusNot(
                "CUST-1", today, "10:00-11:00", "CANCELLED"))
        .thenReturn(true);

    AppointmentDto request =
        new AppointmentDto(
            null,
            "CUST-1",
            "VEH-1",
            "SVC-1",
            null,
            today,
            "10:00-11:00",
            "PENDING",
            null,
            null,
            null);

    assertThatThrownBy(() -> appointmentService.createAppointment(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Customer already has an appointment booked");
  }

  @Test
  @DisplayName("Should reject appointment if technician already has booking for slot")
  void shouldRejectTechnicianDoubleBooking() {
    LocalDate today = LocalDate.now();
    Mockito.when(
            appointmentRepository.existsByCustomerIdAndAppointmentDateAndTimeSlotAndStatusNot(
                "CUST-2", today, "10:00-11:00", "CANCELLED"))
        .thenReturn(false);
    Mockito.when(
            appointmentRepository.existsByTechnicianIdAndAppointmentDateAndTimeSlotAndStatusNot(
                "TECH-1", today, "10:00-11:00", "CANCELLED"))
        .thenReturn(true);

    AppointmentDto request =
        new AppointmentDto(
            null,
            "CUST-2",
            "VEH-1",
            "SVC-1",
            "TECH-1",
            today,
            "10:00-11:00",
            "PENDING",
            null,
            null,
            null);

    assertThatThrownBy(() -> appointmentService.createAppointment(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Technician already has an active appointment assigned");
  }

  @Test
  @DisplayName("Should allow appointment when no conflict exists")
  void shouldAllowAppointmentWhenNoConflict() {
    LocalDate today = LocalDate.now();
    Mockito.when(
            appointmentRepository.existsByCustomerIdAndAppointmentDateAndTimeSlotAndStatusNot(
                "CUST-1", today, "10:00-11:00", "CANCELLED"))
        .thenReturn(false);
    Mockito.when(
            appointmentRepository.existsByTechnicianIdAndAppointmentDateAndTimeSlotAndStatusNot(
                "TECH-1", today, "10:00-11:00", "CANCELLED"))
        .thenReturn(false);

    AppointmentEntity savedEntity =
        AppointmentEntity.builder()
            .id("APT-1001")
            .customerId("CUST-1")
            .vehicleId("VEH-1")
            .serviceId("SVC-1")
            .technicianId("TECH-1")
            .appointmentDate(today)
            .timeSlot("10:00-11:00")
            .status("PENDING")
            .build();

    Mockito.when(appointmentRepository.saveAndFlush(any())).thenReturn(savedEntity);

    AppointmentDto request =
        new AppointmentDto(
            null,
            "CUST-1",
            "VEH-1",
            "SVC-1",
            "TECH-1",
            today,
            "10:00-11:00",
            "PENDING",
            null,
            null,
            null);

    AppointmentDto created = appointmentService.createAppointment(request);
    assertThat(created).isNotNull();
    assertThat(created.id()).isEqualTo("APT-1001");
    verify(schedulingApi).reserveSlot(today, "10:00-11:00");
  }

  @Test
  @DisplayName("Should release the reserved capacity when an appointment is cancelled")
  void shouldReleaseCapacityWhenCancelled() {
    LocalDate today = LocalDate.now();
    AppointmentEntity appointment =
        AppointmentEntity.builder()
            .id("APT-1001")
            .customerId("CUST-1")
            .vehicleId("VEH-1")
            .serviceId("SVC-1")
            .appointmentDate(today)
            .timeSlot("10:00-11:00")
            .status("PENDING")
            .build();
    Mockito.when(appointmentRepository.findById("APT-1001"))
        .thenReturn(java.util.Optional.of(appointment));
    Mockito.when(appointmentRepository.save(any()))
        .thenAnswer(invocation -> invocation.getArgument(0));

    appointmentService.updateStatus("APT-1001", "CANCELLED");

    verify(schedulingApi).releaseSlot(today, "10:00-11:00");
  }
}
