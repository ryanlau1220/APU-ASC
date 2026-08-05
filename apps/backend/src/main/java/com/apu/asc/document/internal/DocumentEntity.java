package com.apu.asc.document.internal;

import com.apu.asc.document.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "documents")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class DocumentEntity {

  @Id private String id;

  @Column(name = "work_order_id", nullable = false)
  private String workOrderId;

  @Column(name = "uploaded_by")
  private String uploadedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "document_type", nullable = false)
  private DocumentType type;

  @Column(name = "file_name", nullable = false, length = 255)
  private String fileName;

  @Column(name = "storage_key", nullable = false, unique = true, length = 255)
  private String storageKey;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "size_bytes", nullable = false)
  private long sizeBytes;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
  }
}
