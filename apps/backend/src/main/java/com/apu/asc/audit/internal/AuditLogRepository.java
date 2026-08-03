package com.apu.asc.audit.internal;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AuditLogRepository extends JpaRepository<AuditLogEntity, AuditLogId> {
  List<AuditLogEntity> findAllByOrderByCreatedAtDesc();

  List<AuditLogEntity> findByUserIdOrderByCreatedAtDesc(String userId);

  List<AuditLogEntity> findByEntityNameOrderByCreatedAtDesc(String entityName);

  List<AuditLogEntity> findByActionTypeOrderByCreatedAtDesc(String actionType);
}
