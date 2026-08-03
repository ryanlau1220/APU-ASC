package com.apu.asc.scheduling;

import java.util.List;

/** Standard customer-facing workshop slots. Managers may also configure an exceptional slot. */
public final class SchedulingTimeSlots {

  private SchedulingTimeSlots() {}

  public static final List<String> STANDARD_SLOTS =
      List.of("09:00 - 10:00 AM", "10:00 - 11:00 AM", "02:00 - 03:00 PM", "04:00 - 05:00 PM");
}
