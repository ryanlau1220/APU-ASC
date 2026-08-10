package com.apu.asc.payment.internal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
class PaymentEntity {

  @Id private String id;

  @Column(name = "appointment_id", unique = true)
  private String appointmentId;

  @Column(name = "work_order_id", unique = true)
  private String workOrderId;

  @Column(name = "customer_id", nullable = false)
  private String customerId;

  @Column(name = "invoice_number", nullable = false, unique = true)
  private String invoiceNumber;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(name = "payment_method", nullable = false)
  private String paymentMethod;

  @Column(name = "payment_status", nullable = false)
  private String paymentStatus;

  @Column(name = "paid_at")
  private Instant paidAt;

  @Column(name = "stripe_checkout_session_id")
  private String stripeCheckoutSessionId;

  @Column(name = "stripe_payment_intent_id")
  private String stripePaymentIntentId;

  @Column(name = "stripe_refund_id")
  private String stripeRefundId;

  @Column(name = "voided_at")
  private Instant voidedAt;

  @Column(name = "void_reason", length = 500)
  private String voidReason;

  @Column(name = "refunded_at")
  private Instant refundedAt;

  @Column(name = "refund_reason", length = 500)
  private String refundReason;

  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @PrePersist
  void prePersist() {
    if (createdAt == null) createdAt = Instant.now();
    if (paymentStatus == null) paymentStatus = "UNPAID";
  }
}
