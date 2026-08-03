package com.apu.asc.quotation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record QuotationDraftRequestDto(
    @NotBlank String workOrderId,
    @Size(max = 2000) String notes,
    @NotNull @FutureOrPresent LocalDate validUntil,
    @NotEmpty List<@Valid QuotationLineRequestDto> items) {}
