package com.apu.asc.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDto(
    String id,
    String appointmentId,
    String customerId,
    String invoiceNumber,
    BigDecimal amount,
    String paymentMethod,
    String paymentStatus,
    Instant paidAt,
    Instant createdAt,
    String workOrderId) {}
