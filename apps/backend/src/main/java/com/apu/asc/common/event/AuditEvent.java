package com.apu.asc.common.event;

public record AuditEvent(
    String userId,
    String actionType,
    String entityName,
    String entityId,
    String details,
    String beforeState,
    String afterState,
    AuditContextSnapshot context) {

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
        AuditContextSnapshot.capture());
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
        AuditContextSnapshot.capture());
  }
}
