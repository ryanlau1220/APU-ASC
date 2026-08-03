package com.apu.asc.audit;

import java.time.Instant;

public record AuditLogDto(
    String id,
    String userId,
    String actionType,
    String entityName,
    String details,
    String actorId,
    String actorUsername,
    String actorRole,
    String correlationId,
    String requestMethod,
    String requestPath,
    String clientIp,
    String userAgent,
    String beforeState,
    String afterState,
    Instant createdAt) {}
