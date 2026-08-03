package com.apu.asc.scheduling;

import java.time.LocalDate;
import java.util.List;

public interface SchedulingApi {
  List<SlotAvailabilityDto> getAvailability(LocalDate appointmentDate);

  SlotAvailabilityDto updateCapacity(SlotCapacityUpdateDto update);

  void reserveSlot(LocalDate appointmentDate, String timeSlot);

  void releaseSlot(LocalDate appointmentDate, String timeSlot);
}
