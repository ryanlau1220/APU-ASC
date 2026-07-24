package com.apu.asc.audit.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
