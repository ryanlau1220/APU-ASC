package com.apu.asc.user.internal;

import com.apu.asc.common.event.AppointmentReminderRequestedEvent;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Delivers next-day appointment emails from the persistent event publication registry. */
@Component
@RequiredArgsConstructor
class AppointmentReminderEmailListener {

  private final UserApi userApi;
  private final EmailService emailService;

  @ApplicationModuleListener(id = "appointment-reminder-email-delivery")
  void on(AppointmentReminderRequestedEvent event) {
    Stream.of(event.customerId(), event.technicianId())
        .filter(java.util.Objects::nonNull)
        .distinct()
        .map(userApi::findById)
        .flatMap(java.util.Optional::stream)
        .filter(user -> "ACTIVE".equalsIgnoreCase(user.status()))
        .forEach(user -> send(event, user));
  }

  private void send(AppointmentReminderRequestedEvent event, UserDto user) {
    emailService.sendAppointmentReminderEmail(
        user.email(),
        user.fullName(),
        event.appointmentDate(),
        event.timeSlot(),
        "TECHNICIAN".equalsIgnoreCase(user.role()));
  }
}
