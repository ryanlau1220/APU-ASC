package com.apu.asc.audit;

import java.util.List;

public interface AuditLogApi {
  List<AuditLogDto> findAllAuditLogs();

  AuditLogDto logAction(String userId, String actionType, String entityName, String details);
}
