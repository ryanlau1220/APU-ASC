package com.apu.asc.appointment.internal;

import com.apu.asc.appointment.AppointmentStatus;
import com.apu.asc.common.event.AppointmentReminderRequestedEvent;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Queues one durable next-day reminder for each confirmed appointment. */
@Component
@ConditionalOnProperty(
    prefix = "appointment-reminders",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true)
class AppointmentReminderScheduler {

  private final AppointmentRepository appointmentRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final Counter queuedReminders;

  @SuppressFBWarnings(
      value = "CT_CONSTRUCTOR_THROW",
      justification = "Fails fast when server-side reminder configuration is invalid.")
  AppointmentReminderScheduler(
      AppointmentRepository appointmentRepository,
      ApplicationEventPublisher eventPublisher,
      MeterRegistry meterRegistry,
      @Value("${appointment-reminders.days-ahead:1}") int daysAhead,
      @Value("${appointment-reminders.time-zone:Asia/Kuala_Lumpur}") String timeZone) {
    this.appointmentRepository = appointmentRepository;
    this.eventPublisher = eventPublisher;
    this.queuedReminders =
        Counter.builder("appointment.reminder.queued")
            .description("Appointment reminders queued for durable delivery")
            .register(meterRegistry);
    if (daysAhead < 1) {
      throw new IllegalArgumentException("Appointment reminder days ahead must be at least one.");
    }
    this.daysAhead = daysAhead;
    this.zoneId = ZoneId.of(timeZone);
  }

  private final int daysAhead;
  private final ZoneId zoneId;

  @Scheduled(
      fixedDelayString = "${appointment-reminders.fixed-delay-ms:900000}",
      initialDelayString = "${appointment-reminders.initial-delay-ms:60000}")
  @Transactional
  void queueDueReminders() {
    LocalDate appointmentDate = LocalDate.now(zoneId).plusDays(daysAhead);
    Instant queuedAt = Instant.now();
    appointmentRepository
        .findByAppointmentDateAndStatusAndReminderQueuedAtIsNull(
            appointmentDate, AppointmentStatus.CONFIRMED.name())
        .forEach(
            appointment -> {
              if (appointmentRepository.claimReminder(
                      appointment.getId(), AppointmentStatus.CONFIRMED.name(), queuedAt)
                  == 1) {
                eventPublisher.publishEvent(
                    new AppointmentReminderRequestedEvent(
                        null,
                        appointment.getId(),
                        appointment.getCustomerId(),
                        appointment.getTechnicianId(),
                        appointment.getAppointmentDate(),
                        appointment.getTimeSlot(),
                        queuedAt));
                queuedReminders.increment();
              }
            });
  }
}
