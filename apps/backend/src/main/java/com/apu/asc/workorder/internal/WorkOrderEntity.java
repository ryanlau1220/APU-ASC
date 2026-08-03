package com.apu.asc.workorder.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "work_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class WorkOrderEntity {

  @Id private String id;

  @Column(name = "appointment_id", unique = true)
  private String appointmentId;

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Column(name = "vehicle_id", nullable = false)
  private String vehicleId;

  @Column(name = "service_id", nullable = false)
  private String serviceId;

  @Column(name = "technician_id")
  private String technicianId;

  @Column(nullable = false)
  private String status;

  @Column(name = "intake_notes", columnDefinition = "TEXT")
  private String intakeNotes;

  @Column(name = "diagnostic_notes", columnDefinition = "TEXT")
  private String diagnosticNotes;

  @Column(name = "opened_at")
  private Instant openedAt;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (openedAt == null) openedAt = now;
    if (status == null) status = "OPEN";
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}
