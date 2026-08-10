package com.apu.asc.common.event;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Durable request to notify people about a confirmed appointment occurring tomorrow. */
public record AppointmentReminderRequestedEvent(
    UUID eventId,
    String appointmentId,
    String customerId,
    String technicianId,
    LocalDate appointmentDate,
    String timeSlot,
    Instant occurredAt) {

  public AppointmentReminderRequestedEvent {
    eventId = eventId == null ? UUID.randomUUID() : eventId;
    appointmentId = requireId(appointmentId, "appointment ID");
    customerId = requireId(customerId, "customer ID");
    technicianId = technicianId == null || technicianId.isBlank() ? null : technicianId.trim();
    if (appointmentDate == null) {
      throw new IllegalArgumentException("An appointment date is required.");
    }
    if (timeSlot == null || timeSlot.isBlank()) {
      throw new IllegalArgumentException("An appointment time slot is required.");
    }
    timeSlot = timeSlot.trim();
    occurredAt = occurredAt == null ? Instant.now() : occurredAt;
  }

  private static String requireId(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("An " + field + " is required.");
    }
    return value.trim();
  }
}
