package com.apu.asc.workorder.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.apu.asc.workorder.WorkOrderDto;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceImplTest {

  @Mock private WorkOrderRepository workOrderRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private WorkOrderServiceImpl workOrderService;

  @BeforeEach
  void setUp() {
    workOrderService = new WorkOrderServiceImpl(workOrderRepository, eventPublisher);
  }

  @Test
  void createsOneWorkOrderForAnAppointment() {
    WorkOrderDto request = workOrder("APT-1", "OPEN");
    when(workOrderRepository.findByAppointmentId("APT-1")).thenReturn(Optional.empty());
    when(workOrderRepository.save(any()))
        .thenAnswer(
            invocation -> {
              WorkOrderEntity entity = invocation.getArgument(0);
              entity.prePersist();
              return entity;
            });

    WorkOrderDto created = workOrderService.createWorkOrder(request);

    assertThat(created.id()).startsWith("WO-");
    assertThat(created.appointmentId()).isEqualTo("APT-1");
    assertThat(created.status()).isEqualTo("OPEN");
    verify(workOrderRepository).save(any());
  }

  @Test
  void rejectsASecondWorkOrderForTheSameAppointment() {
    when(workOrderRepository.findByAppointmentId("APT-1"))
        .thenReturn(Optional.of(WorkOrderEntity.builder().id("WO-1").build()));

    assertThatThrownBy(() -> workOrderService.createWorkOrder(workOrder("APT-1", "OPEN")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("already exists");
  }

  @Test
  void recordsStartAndCompletionTimesOnExecutionStatusChanges() {
    WorkOrderEntity entity =
        WorkOrderEntity.builder()
            .id("WO-1")
            .customerId("USR-1")
            .vehicleId("VEH-1")
            .serviceId("SVC-1")
            .status("OPEN")
            .build();
    when(workOrderRepository.findById("WO-1")).thenReturn(Optional.of(entity));
    when(workOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    WorkOrderDto started = workOrderService.updateStatus("WO-1", "IN_PROGRESS");
    WorkOrderDto completed = workOrderService.updateStatus("WO-1", "COMPLETED");

    assertThat(started.startedAt()).isNotNull();
    assertThat(completed.completedAt()).isNotNull();
  }

  private WorkOrderDto workOrder(String appointmentId, String status) {
    return new WorkOrderDto(
        null,
        appointmentId,
        "USR-1",
        "VEH-1",
        "SVC-1",
        "USR-TECH",
        status,
        null,
        null,
        null,
        null,
        null,
        null,
        null);
  }
}
