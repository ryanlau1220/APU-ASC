package com.apu.asc.common.event;

public record AuditEvent(
    String userId, String actionType, String entityName, String entityId, String details) {}
