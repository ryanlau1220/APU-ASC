package com.apu.asc.audit.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "audit_logs")
@IdClass(AuditLogId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Immutable
class AuditLogEntity {

  @Id private String id;

  @Column(name = "user_id")
  private String userId;

  @Column(name = "action_type", nullable = false)
  private String actionType;

  @Column(name = "entity_name", nullable = false)
  private String entityName;

  @Column(columnDefinition = "TEXT")
  private String details;

  @Column(name = "actor_id", updatable = false)
  private String actorId;

  @Column(name = "actor_username", updatable = false)
  private String actorUsername;

  @Column(name = "actor_role", updatable = false)
  private String actorRole;

  @Column(name = "correlation_id", updatable = false)
  private String correlationId;

  @Column(name = "request_method", updatable = false)
  private String requestMethod;

  @Column(name = "request_path", updatable = false)
  private String requestPath;

  @Column(name = "client_ip", updatable = false)
  private String clientIp;

  @Column(name = "user_agent", updatable = false)
  private String userAgent;

  @Column(name = "before_state", columnDefinition = "TEXT", updatable = false)
  private String beforeState;

  @Column(name = "after_state", columnDefinition = "TEXT", updatable = false)
  private String afterState;

  @Id
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
