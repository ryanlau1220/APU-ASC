package com.apu.asc.appointment.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apu.asc.appointment.AppointmentStatus;
import com.apu.asc.common.event.AppointmentReminderRequestedEvent;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AppointmentReminderSchedulerTest {

  @Mock private AppointmentRepository appointmentRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  @Test
  void queuesOnlyRemindersClaimedByThisSchedulerRun() {
    ZoneId zoneId = ZoneId.of("Asia/Kuala_Lumpur");
    LocalDate tomorrow = LocalDate.now(zoneId).plusDays(1);
    AppointmentEntity claimed = appointment("APT-1", tomorrow);
    AppointmentEntity alreadyClaimed = appointment("APT-2", tomorrow);
    when(appointmentRepository.findByAppointmentDateAndStatusAndReminderQueuedAtIsNull(
            tomorrow, AppointmentStatus.CONFIRMED.name()))
        .thenReturn(List.of(claimed, alreadyClaimed));
    when(appointmentRepository.claimReminder(eq("APT-1"), eq("CONFIRMED"), any())).thenReturn(1);
    when(appointmentRepository.claimReminder(eq("APT-2"), eq("CONFIRMED"), any())).thenReturn(0);

    new AppointmentReminderScheduler(
            appointmentRepository, eventPublisher, new SimpleMeterRegistry(), 1, zoneId.getId())
        .queueDueReminders();

    ArgumentCaptor<AppointmentReminderRequestedEvent> event =
        ArgumentCaptor.forClass(AppointmentReminderRequestedEvent.class);
    verify(eventPublisher).publishEvent(event.capture());
    assertThat(event.getValue().appointmentId()).isEqualTo("APT-1");
    assertThat(event.getValue().customerId()).isEqualTo("USR-CUSTOMER");
  }

  private AppointmentEntity appointment(String id, LocalDate appointmentDate) {
    return AppointmentEntity.builder()
        .id(id)
        .customerId("USR-CUSTOMER")
        .technicianId("USR-TECHNICIAN")
        .vehicleId("VEH-1")
        .serviceId("SVC-1")
        .appointmentDate(appointmentDate)
        .timeSlot("09:00 - 10:00 AM")
        .status("CONFIRMED")
        .build();
  }
}
