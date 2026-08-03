package com.apu.asc.scheduling.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apu.asc.common.event.LiveUpdateEvent;
import com.apu.asc.scheduling.SlotAvailabilityDto;
import com.apu.asc.scheduling.SlotCapacityUpdateDto;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SchedulingServiceImplTest {

  private static final LocalDate DATE = LocalDate.of(2026, 8, 10);
  private static final String SLOT = "09:00 - 10:00 AM";

  @Mock private AppointmentSlotCapacityStore capacityStore;
  @Mock private ApplicationEventPublisher eventPublisher;

  private SchedulingServiceImpl schedulingService;

  @BeforeEach
  void setUp() {
    schedulingService = new SchedulingServiceImpl(capacityStore, eventPublisher);
    ReflectionTestUtils.setField(schedulingService, "defaultSlotCapacity", 3);
  }

  @Test
  void rejectsAReservationWhenTheAtomicStoreReportsNoRemainingCapacity() {
    when(capacityStore.reserve(DATE, SLOT, 3)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> schedulingService.reserveSlot(DATE, SLOT))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("fully booked");
  }

  @Test
  void preventsReducingCapacityBelowExistingReservations() {
    when(capacityStore.updateCapacity(DATE, SLOT, 1)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> schedulingService.updateCapacity(new SlotCapacityUpdateDto(DATE, SLOT, 1)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already reserved");
  }

  @Test
  void publishesAvailabilityUpdateAfterChangingCapacity() {
    SlotAvailabilityDto availability = new SlotAvailabilityDto(DATE, SLOT, 4, 1, 3, true);
    when(capacityStore.updateCapacity(DATE, SLOT, 4)).thenReturn(Optional.of(availability));

    schedulingService.updateCapacity(new SlotCapacityUpdateDto(DATE, SLOT, 4));

    ArgumentCaptor<LiveUpdateEvent> eventCaptor = ArgumentCaptor.forClass(LiveUpdateEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    LiveUpdateEvent event = eventCaptor.getValue();
    assertThat(event.topic()).isEqualTo("scheduling");
    assertThat(event.resourceId()).isNull();
    assertThat(event.audienceUserIds()).isEmpty();
    assertThat(event.audienceRoles())
        .containsExactlyInAnyOrder("CUSTOMER", "TECHNICIAN", "STAFF", "MANAGER");
    assertThat(event.occurredAt()).isNotNull();
  }
}
