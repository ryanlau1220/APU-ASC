package com.apu.asc.vehicle.internal;

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
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class VehicleEntity {

  @Id private String id;

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Column(name = "license_plate", nullable = false, unique = true)
  private String licensePlate;

  @Column(nullable = false)
  private String make;

  @Column(nullable = false)
  private String model;

  @Column(name = "year_of_manufacture", nullable = false)
  private Integer yearOfManufacture;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
