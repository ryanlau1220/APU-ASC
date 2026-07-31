package com.apu.asc.audit;

import java.util.List;

public interface AuditLogApi {
  List<AuditLogDto> findAllAuditLogs();

  List<AuditLogDto> findByUserId(String userId);

  List<AuditLogDto> findByEntityName(String entityName);

  List<AuditLogDto> findByActionType(String actionType);

  AuditLogDto logAction(
      String userId, String actionType, String entityName, String entityId, String details);
}
