package com.apu.asc.scheduling.internal;

import com.apu.asc.common.event.LiveUpdateEvent;
import com.apu.asc.scheduling.SchedulingApi;
import com.apu.asc.scheduling.SchedulingTimeSlots;
import com.apu.asc.scheduling.SlotAvailabilityDto;
import com.apu.asc.scheduling.SlotCapacityUpdateDto;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class SchedulingServiceImpl implements SchedulingApi {

  private final AppointmentSlotCapacityStore capacityStore;
  private final ApplicationEventPublisher eventPublisher;

  @Value("${scheduling.default-slot-capacity:3}")
  private int defaultSlotCapacity;

  @Override
  @Transactional(readOnly = true)
  public List<SlotAvailabilityDto> getAvailability(LocalDate appointmentDate) {
    requireDate(appointmentDate);
    if (defaultSlotCapacity < 1) {
      throw new IllegalStateException("Scheduling default capacity must be at least one.");
    }
    Map<String, SlotAvailabilityDto> configuredSlots = new HashMap<>();
    capacityStore
        .findByDate(appointmentDate)
        .forEach(slot -> configuredSlots.put(slot.timeSlot(), slot));
    return Stream.concat(
            SchedulingTimeSlots.STANDARD_SLOTS.stream()
                .map(
                    timeSlot -> {
                      SlotAvailabilityDto configured = configuredSlots.remove(timeSlot);
                      return configured != null
                          ? configured
                          : new SlotAvailabilityDto(
                              appointmentDate,
                              timeSlot,
                              defaultSlotCapacity,
                              0,
                              defaultSlotCapacity,
                              true);
                    }),
            configuredSlots.values().stream()
                .sorted(Comparator.comparing(SlotAvailabilityDto::timeSlot)))
        .toList();
  }

  @Override
  @Transactional
  public SlotAvailabilityDto updateCapacity(SlotCapacityUpdateDto update) {
    requireDate(update.appointmentDate());
    requireTimeSlot(update.timeSlot());
    if (update.capacity() < 1) {
      throw new IllegalArgumentException("Slot capacity must be at least one.");
    }
    SlotAvailabilityDto availability =
        capacityStore
            .updateCapacity(update.appointmentDate(), update.timeSlot(), update.capacity())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Capacity cannot be lower than the appointments already reserved."));
    eventPublisher.publishEvent(
        LiveUpdateEvent.forRoles("scheduling", null, "CUSTOMER", "TECHNICIAN", "STAFF", "MANAGER"));
    return availability;
  }

  @Override
  @Transactional
  public void reserveSlot(LocalDate appointmentDate, String timeSlot) {
    requireDate(appointmentDate);
    requireTimeSlot(timeSlot);
    if (defaultSlotCapacity < 1) {
      throw new IllegalStateException("Scheduling default capacity must be at least one.");
    }
    if (capacityStore.reserve(appointmentDate, timeSlot, defaultSlotCapacity).isEmpty()) {
      throw new IllegalArgumentException("This appointment time slot is fully booked.");
    }
  }

  @Override
  @Transactional
  public void releaseSlot(LocalDate appointmentDate, String timeSlot) {
    requireDate(appointmentDate);
    requireTimeSlot(timeSlot);
    capacityStore.release(appointmentDate, timeSlot);
  }

  private void requireDate(LocalDate appointmentDate) {
    if (appointmentDate == null) {
      throw new IllegalArgumentException("An appointment date is required.");
    }
  }

  private void requireTimeSlot(String timeSlot) {
    if (timeSlot == null || timeSlot.isBlank()) {
      throw new IllegalArgumentException("A time slot is required.");
    }
  }
}
