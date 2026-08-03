package com.apu.asc.scheduling;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Manager-provided capacity override for one workshop slot. */
public record SlotCapacityUpdateDto(
    @NotNull LocalDate appointmentDate, @NotBlank String timeSlot, @Min(1) int capacity) {}
