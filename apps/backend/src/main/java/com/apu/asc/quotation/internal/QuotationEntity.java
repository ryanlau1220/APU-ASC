package com.apu.asc.quotation.internal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quotations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class QuotationEntity {

  @Id private String id;

  @Column(name = "quote_number", nullable = false, unique = true)
  private String quoteNumber;

  @Column(name = "work_order_id", nullable = false)
  private String workOrderId;

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Column(nullable = false)
  private int revision;

  @Column(nullable = false)
  private String status;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "response_notes", columnDefinition = "TEXT")
  private String responseNotes;

  @Column(name = "valid_until")
  private LocalDate validUntil;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal taxAmount;

  @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "submitted_at")
  private Instant submittedAt;

  @Column(name = "responded_at")
  private Instant respondedAt;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Builder.Default
  @OneToMany(mappedBy = "quotation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<QuotationLineEntity> items = new ArrayList<>();

  void replaceItems(List<QuotationLineEntity> replacement) {
    items.clear();
    replacement.forEach(
        item -> {
          item.setQuotation(this);
          items.add(item);
        });
  }

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }
}
