package com.apu.asc.audit.internal;

import com.apu.asc.audit.AuditLogApi;
import com.apu.asc.audit.AuditLogDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class AuditLogServiceImpl implements AuditLogApi {

  private final AuditLogRepository auditLogRepository;

  @Override
  @Transactional(readOnly = true)
  public List<AuditLogDto> findAllAuditLogs() {
    return auditLogRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional
  public AuditLogDto logAction(
      final String userId, final String actionType, final String entityName, final String details) {
    AuditLogEntity log =
        AuditLogEntity.builder()
            .id("AUD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
            .userId(userId)
            .actionType(actionType)
            .entityName(entityName)
            .details(details)
            .build();
    return toDto(auditLogRepository.save(log));
  }

  private AuditLogDto toDto(AuditLogEntity entity) {
    return new AuditLogDto(
        entity.getId(),
        entity.getUserId(),
        entity.getActionType(),
        entity.getEntityName(),
        entity.getDetails(),
        entity.getCreatedAt());
  }
}
