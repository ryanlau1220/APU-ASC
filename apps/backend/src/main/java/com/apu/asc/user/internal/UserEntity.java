package com.apu.asc.user.internal;

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
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class UserEntity {

  @Id private String id;

  @Column(name = "keycloak_id", unique = true)
  private String keycloakId;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "contact_number", nullable = false)
  private String contactNumber;

  @Column(nullable = false)
  private String role;

  @Column(nullable = false)
  private String status;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
    updatedAt = Instant.now();
    if (status == null) status = "ACTIVE";
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}
