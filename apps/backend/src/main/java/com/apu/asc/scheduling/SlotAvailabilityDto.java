package com.apu.asc.scheduling;

import java.time.LocalDate;

/** Capacity remaining for a bookable workshop time slot. */
public record SlotAvailabilityDto(
    LocalDate appointmentDate,
    String timeSlot,
    int capacity,
    int reserved,
    int available,
    boolean bookable) {}
