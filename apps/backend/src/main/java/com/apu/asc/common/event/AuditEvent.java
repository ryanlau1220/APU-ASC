package com.apu.asc.common.event;

import java.time.Instant;
import java.util.UUID;

public record AuditEvent(
    String userId,
    String actionType,
    String entityName,
    String entityId,
    String details,
    String beforeState,
    String afterState,
    AuditContextSnapshot context,
    UUID eventId,
    Instant occurredAt) {

  public AuditEvent {
    eventId = eventId == null ? UUID.randomUUID() : eventId;
    occurredAt = occurredAt == null ? Instant.now() : occurredAt;
  }

  public AuditEvent(
      String userId, String actionType, String entityName, String entityId, String details) {
    this(
        userId,
        actionType,
        entityName,
        entityId,
        details,
        null,
        null,
        AuditContextSnapshot.capture(),
        null,
        null);
  }

  public AuditEvent(
      String userId,
      String actionType,
      String entityName,
      String entityId,
      String details,
      String beforeState,
      String afterState) {
    this(
        userId,
        actionType,
        entityName,
        entityId,
        details,
        beforeState,
        afterState,
        AuditContextSnapshot.capture(),
        null,
        null);
  }

  public AuditEvent(
      String userId,
      String actionType,
      String entityName,
      String entityId,
      String details,
      String beforeState,
      String afterState,
      AuditContextSnapshot context) {
    this(
        userId,
        actionType,
        entityName,
        entityId,
        details,
        beforeState,
        afterState,
        context,
        null,
        null);
  }
}
