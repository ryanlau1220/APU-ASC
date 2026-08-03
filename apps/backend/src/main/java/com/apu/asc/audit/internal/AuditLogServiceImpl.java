package com.apu.asc.audit.internal;

import com.apu.asc.audit.AuditLogApi;
import com.apu.asc.audit.AuditLogDto;
import com.apu.asc.common.event.AuditContextSnapshot;
import com.apu.asc.common.event.AuditEvent;
import java.time.Instant;
import java.util.List;
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
    return auditLogRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditLogDto> findByUserId(final String userId) {
    return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditLogDto> findByEntityName(final String entityName) {
    return auditLogRepository.findByEntityNameOrderByCreatedAtDesc(entityName).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AuditLogDto> findByActionType(final String actionType) {
    return auditLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType).stream()
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional
  public AuditLogDto logAction(final AuditEvent event) {
    String logId = "AUD-" + event.eventId();
    Instant createdAt = event.occurredAt();
    AuditLogId auditLogId = new AuditLogId(logId, createdAt);
    var existing = auditLogRepository.findById(auditLogId);
    if (existing.isPresent()) {
      return toDto(existing.get());
    }
    String formattedDetails =
        event.entityId() != null
            ? "[Entity ID: " + event.entityId() + "] " + event.details()
            : event.details();
    AuditContextSnapshot context =
        event.context() != null ? event.context() : AuditContextSnapshot.capture();

    AuditLogEntity log =
        AuditLogEntity.builder()
            .id(logId)
            .userId(event.userId())
            .actionType(event.actionType())
            .entityName(event.entityName())
            .details(formattedDetails)
            .actorId(context.actorId())
            .actorUsername(context.actorUsername())
            .actorRole(context.actorRole())
            .correlationId(context.correlationId())
            .requestMethod(context.requestMethod())
            .requestPath(context.requestPath())
            .clientIp(context.clientIp())
            .userAgent(context.userAgent())
            .beforeState(event.beforeState())
            .afterState(event.afterState())
            .createdAt(createdAt)
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
        entity.getActorId(),
        entity.getActorUsername(),
        entity.getActorRole(),
        entity.getCorrelationId(),
        entity.getRequestMethod(),
        entity.getRequestPath(),
        entity.getClientIp(),
        entity.getUserAgent(),
        entity.getBeforeState(),
        entity.getAfterState(),
        entity.getCreatedAt());
  }
}
