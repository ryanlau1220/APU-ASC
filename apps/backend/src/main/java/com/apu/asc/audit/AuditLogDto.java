package com.apu.asc.audit;

import java.time.Instant;

public record AuditLogDto(
    String id,
    String userId,
    String actionType,
    String entityName,
    String details,
    Instant createdAt) {}
