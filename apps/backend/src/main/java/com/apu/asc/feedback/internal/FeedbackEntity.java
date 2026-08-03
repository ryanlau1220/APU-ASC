package com.apu.asc.feedback.internal;

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
@Table(name = "feedbacks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class FeedbackEntity {

  @Id private String id;

  @Column(name = "appointment_id")
  private String appointmentId;

  @Column(name = "work_order_id")
  private String workOrderId;

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Column(name = "technician_id")
  private String technicianId;

  private Integer rating;

  @Column(columnDefinition = "TEXT")
  private String comments;

  @Column(name = "technician_diagnostic_notes", columnDefinition = "TEXT")
  private String technicianDiagnosticNotes;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
