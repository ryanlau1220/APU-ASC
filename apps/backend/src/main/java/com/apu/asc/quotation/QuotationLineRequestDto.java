package com.apu.asc.quotation;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

/** A requested line item. Amounts are calculated server-side and never accepted from the client. */
public record QuotationLineRequestDto(
    @NotBlank String description,
    @DecimalMin(value = "0.01") @Digits(integer = 8, fraction = 2) BigDecimal quantity,
    @DecimalMin(value = "0.00") @Digits(integer = 10, fraction = 2) BigDecimal unitPrice) {}
