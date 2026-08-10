package com.apu.asc.appointment.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class AppointmentEntity {

  @Id private String id;

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Column(name = "vehicle_id", nullable = false)
  private String vehicleId;

  @Column(name = "service_id", nullable = false)
  private String serviceId;

  @Column(name = "technician_id")
  private String technicianId;

  @Column(name = "appointment_date", nullable = false)
  private LocalDate appointmentDate;

  @Column(name = "time_slot", nullable = false)
  private String timeSlot;

  @Column(nullable = false)
  private String status;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Column(name = "reminder_queued_at")
  private Instant reminderQueuedAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
    updatedAt = Instant.now();
    if (status == null) status = "PENDING";
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}
