package com.apu.asc.quotation;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record QuotationDecisionRequestDto(
    @NotNull Decision decision, @Size(max = 1000) String responseNotes) {
  public enum Decision {
    APPROVE,
    REJECT
  }
}
