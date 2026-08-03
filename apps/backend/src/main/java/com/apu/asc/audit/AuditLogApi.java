package com.apu.asc.audit;

import com.apu.asc.common.event.AuditEvent;
import java.util.List;

public interface AuditLogApi {
  List<AuditLogDto> findAllAuditLogs();

  List<AuditLogDto> findByUserId(String userId);

  List<AuditLogDto> findByEntityName(String entityName);

  List<AuditLogDto> findByActionType(String actionType);

  AuditLogDto logAction(AuditEvent event);

  default AuditLogDto logAction(
      String userId, String actionType, String entityName, String entityId, String details) {
    return logAction(new AuditEvent(userId, actionType, entityName, entityId, details));
  }
}
