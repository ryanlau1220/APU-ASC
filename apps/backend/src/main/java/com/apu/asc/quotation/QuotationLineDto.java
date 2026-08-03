package com.apu.asc.quotation;

import java.math.BigDecimal;

public record QuotationLineDto(
    String id,
    String description,
    BigDecimal quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal) {}
