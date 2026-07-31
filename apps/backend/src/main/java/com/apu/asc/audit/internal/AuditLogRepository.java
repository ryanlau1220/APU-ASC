package com.apu.asc.audit.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AuditLogRepository extends JpaRepository<AuditLogEntity, AuditLogId> {
  List<AuditLogEntity> findByUserId(String userId);

  List<AuditLogEntity> findByEntityName(String entityName);

  List<AuditLogEntity> findByActionType(String actionType);
}
