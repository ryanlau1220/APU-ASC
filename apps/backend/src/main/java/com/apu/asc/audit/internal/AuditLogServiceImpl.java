package com.apu.asc.audit.internal;

import com.apu.asc.audit.AuditLogApi;
import com.apu.asc.audit.AuditLogDto;
import java.time.Instant;
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
  @Transactional(readOnly = true)
  public List<AuditLogDto> findByUserId(final String userId) {
    return auditLogRepository.findByUserId(userId).stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditLogDto> findByEntityName(final String entityName) {
    return auditLogRepository.findByEntityName(entityName).stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditLogDto> findByActionType(final String actionType) {
    return auditLogRepository.findByActionType(actionType).stream().map(this::toDto).toList();
  }

  @Override
  @Transactional
  public AuditLogDto logAction(
      final String userId,
      final String actionType,
      final String entityName,
      final String entityId,
      final String details) {
    String logId = "AUD-" + UUID.randomUUID().toString();
    String formattedDetails =
        entityId != null ? "[Entity ID: " + entityId + "] " + details : details;

    AuditLogEntity log =
        AuditLogEntity.builder()
            .id(logId)
            .userId(userId)
            .actionType(actionType)
            .entityName(entityName)
            .details(formattedDetails)
            .createdAt(Instant.now())
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
