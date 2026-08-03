package com.apu.asc.workorder;

import java.time.Instant;

/** A workshop execution record. An appointment is optional to support walk-in work. */
public record WorkOrderDto(
    String id,
    String appointmentId,
    String customerId,
    String vehicleId,
    String serviceId,
    String technicianId,
    String status,
    String intakeNotes,
    String diagnosticNotes,
    Instant openedAt,
    Instant startedAt,
    Instant completedAt,
    Instant createdAt,
    Instant updatedAt) {}
