package com.apu.asc.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Manager-supplied rationale retained with an invoice void or full refund. */
public record PaymentExceptionRequest(
    @NotBlank(message = "A reason is required.")
        @Size(max = 500, message = "Reason must not exceed 500 characters.")
        String reason) {}
