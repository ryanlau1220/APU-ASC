package com.apu.asc.quotation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record QuotationDto(
    String id,
    String quoteNumber,
    String workOrderId,
    String customerId,
    int revision,
    String status,
    String notes,
    String responseNotes,
    LocalDate validUntil,
    BigDecimal subtotal,
    BigDecimal taxAmount,
    BigDecimal totalAmount,
    Instant submittedAt,
    Instant respondedAt,
    Instant createdAt,
    Instant updatedAt,
    List<QuotationLineDto> items) {}
