package com.apu.asc.appointment;

import java.time.Instant;
import java.time.LocalDate;

public record AppointmentDto(
    String id,
    String customerId,
    String vehicleId,
    String serviceId,
    String technicianId,
    LocalDate appointmentDate,
    String timeSlot,
    String status,
    String notes,
    Instant createdAt,
    Instant updatedAt) {}
