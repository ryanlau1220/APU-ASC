package com.apu.asc.feedback;

import java.time.Instant;

public record FeedbackDto(
    String id,
    String appointmentId,
    String customerId,
    String technicianId,
    Integer rating,
    String comments,
    String technicianDiagnosticNotes,
    Instant createdAt) {}
