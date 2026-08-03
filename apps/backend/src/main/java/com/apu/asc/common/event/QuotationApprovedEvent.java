package com.apu.asc.common.event;

import java.time.Instant;

/** Published when a customer approves a quotation for a work order. */
public record QuotationApprovedEvent(String workOrderId, String quotationId, Instant approvedAt) {}
