package com.apu.asc.payment;

import java.math.BigDecimal;
import java.time.Instant;

/** Immutable data used to render a customer-facing receipt or credit note. */
public record PaymentRecordDto(
    String id,
    String invoiceNumber,
    BigDecimal amount,
    String paymentMethod,
    String paymentStatus,
    Instant paidAt,
    Instant voidedAt,
    String voidReason,
    Instant refundedAt,
    String refundReason,
    Instant createdAt) {}
