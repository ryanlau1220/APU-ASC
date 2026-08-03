package com.apu.asc.workorder.internal;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.common.event.SseBroadcastEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.workorder.WorkOrderApi;
import com.apu.asc.workorder.WorkOrderDto;
import com.apu.asc.workorder.WorkOrderStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class WorkOrderServiceImpl implements WorkOrderApi {

  private final WorkOrderRepository workOrderRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional(readOnly = true)
  public List<WorkOrderDto> findAllWorkOrders() {
    return workOrderRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public WorkOrderDto getWorkOrderById(String id) {
    return workOrderRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Work order", id));
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<WorkOrderDto> findByAppointmentId(String appointmentId) {
    return workOrderRepository.findByAppointmentId(appointmentId).map(this::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public List<WorkOrderDto> findByCustomer(String customerId) {
    return workOrderRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<WorkOrderDto> findByTechnician(String technicianId) {
    return workOrderRepository.findByTechnicianId(technicianId).stream().map(this::toDto).toList();
  }

  @Override
  @Transactional
  public WorkOrderDto createWorkOrder(WorkOrderDto workOrderDto) {
    if (workOrderDto.appointmentId() != null
        && workOrderRepository.findByAppointmentId(workOrderDto.appointmentId()).isPresent()) {
      throw new IllegalArgumentException("A work order already exists for this appointment.");
    }

    WorkOrderEntity entity =
        WorkOrderEntity.builder()
            .id(workOrderDto.id() != null ? workOrderDto.id() : "WO-" + UUID.randomUUID())
            .appointmentId(workOrderDto.appointmentId())
            .customerId(workOrderDto.customerId())
            .vehicleId(workOrderDto.vehicleId())
            .serviceId(workOrderDto.serviceId())
            .technicianId(workOrderDto.technicianId())
            .status(WorkOrderStatus.OPEN.name())
            .intakeNotes(workOrderDto.intakeNotes())
            .diagnosticNotes(workOrderDto.diagnosticNotes())
            .build();
    WorkOrderDto created = toDto(workOrderRepository.save(entity));
    publish(created, "WORK_ORDER_CREATED", "Opened work order " + created.id());
    return created;
  }

  @Override
  @Transactional
  public WorkOrderDto updateWorkOrder(String id, WorkOrderDto workOrderDto) {
    WorkOrderEntity entity = findEntity(id);
    if (workOrderDto.technicianId() != null) entity.setTechnicianId(workOrderDto.technicianId());
    if (workOrderDto.intakeNotes() != null) entity.setIntakeNotes(workOrderDto.intakeNotes());
    if (workOrderDto.diagnosticNotes() != null)
      entity.setDiagnosticNotes(workOrderDto.diagnosticNotes());
    WorkOrderDto updated = toDto(workOrderRepository.save(entity));
    publish(updated, "WORK_ORDER_UPDATED", "Updated work order details");
    return updated;
  }

  @Override
  @Transactional
  public WorkOrderDto updateStatus(String id, String status) {
    WorkOrderEntity entity = findEntity(id);
    String beforeState = workOrderAuditState(entity);
    WorkOrderStatus target = WorkOrderStatus.fromString(status);
    WorkOrderStatus current = WorkOrderStatus.fromString(entity.getStatus());
    current.requireTransitionTo(target);
    if (current == target) {
      return toDto(entity);
    }
    if (target == WorkOrderStatus.IN_PROGRESS && entity.getApprovedQuotationId() == null) {
      throw new IllegalArgumentException(
          "An approved customer quotation is required before work can begin.");
    }
    entity.setStatus(target.name());
    Instant now = Instant.now();
    if (target == WorkOrderStatus.IN_PROGRESS && entity.getStartedAt() == null)
      entity.setStartedAt(now);
    if (target == WorkOrderStatus.COMPLETED && entity.getCompletedAt() == null)
      entity.setCompletedAt(now);
    WorkOrderDto updated = toDto(workOrderRepository.save(entity));
    publish(
        updated,
        "WORK_ORDER_STATUS_UPDATED",
        "Updated work-order execution status.",
        beforeState,
        workOrderAuditState(entity));
    return updated;
  }

  @Override
  @Transactional
  public WorkOrderDto updateDiagnosticNotes(String id, String diagnosticNotes) {
    WorkOrderEntity entity = findEntity(id);
    entity.setDiagnosticNotes(diagnosticNotes);
    WorkOrderDto updated = toDto(workOrderRepository.save(entity));
    publish(updated, "WORK_ORDER_DIAGNOSTICS_UPDATED", "Updated diagnostic notes");
    return updated;
  }

  @Override
  @Transactional
  public void deleteWorkOrder(String id) {
    WorkOrderEntity entity = findEntity(id);
    workOrderRepository.delete(entity);
    eventPublisher.publishEvent(new SseBroadcastEvent("work-orders"));
    eventPublisher.publishEvent(
        new AuditEvent(
            entity.getCustomerId(), "WORK_ORDER_DELETED", "WORK_ORDER", id, "Deleted work order"));
  }

  private WorkOrderEntity findEntity(String id) {
    return workOrderRepository
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Work order", id));
  }

  private void publish(WorkOrderDto workOrder, String action, String details) {
    publish(workOrder, action, details, null, null);
  }

  private void publish(
      WorkOrderDto workOrder,
      String action,
      String details,
      String beforeState,
      String afterState) {
    eventPublisher.publishEvent(new SseBroadcastEvent("work-orders"));
    eventPublisher.publishEvent(
        new AuditEvent(
            workOrder.customerId(),
            action,
            "WORK_ORDER",
            workOrder.id(),
            details,
            beforeState,
            afterState));
  }

  private String workOrderAuditState(WorkOrderEntity entity) {
    return "status="
        + entity.getStatus()
        + "; technicianId="
        + entity.getTechnicianId()
        + "; startedAt="
        + entity.getStartedAt()
        + "; completedAt="
        + entity.getCompletedAt();
  }

  private WorkOrderDto toDto(WorkOrderEntity entity) {
    return new WorkOrderDto(
        entity.getId(),
        entity.getAppointmentId(),
        entity.getCustomerId(),
        entity.getVehicleId(),
        entity.getServiceId(),
        entity.getTechnicianId(),
        entity.getStatus(),
        entity.getIntakeNotes(),
        entity.getDiagnosticNotes(),
        entity.getOpenedAt(),
        entity.getStartedAt(),
        entity.getCompletedAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
