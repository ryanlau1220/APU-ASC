package com.apu.asc.workorder.internal;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
import com.apu.asc.vehicle.VehicleApi;
import com.apu.asc.vehicle.VehicleDto;
import com.apu.asc.workorder.WorkOrderApi;
import com.apu.asc.workorder.WorkOrderDto;
import com.apu.asc.workorder.WorkOrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/work-orders")
@RequiredArgsConstructor
@Tag(name = "Work Orders", description = "Workshop execution, assignment, and diagnostic APIs")
class WorkOrderController {

  private final WorkOrderApi workOrderApi;
  private final AppointmentApi appointmentApi;
  private final VehicleApi vehicleApi;
  private final UserApi userApi;
  private final CurrentUserService currentUserService;
  private final AccessPolicy accessPolicy;

  @GetMapping
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(operationId = "getAllWorkOrders", summary = "Get all work orders")
  public ResponseEntity<List<WorkOrderDto>> getAllWorkOrders() {
    return ResponseEntity.ok(workOrderApi.findAllWorkOrders());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(operationId = "getWorkOrderById", summary = "Get work order by ID")
  public ResponseEntity<WorkOrderDto> getWorkOrderById(
      @PathVariable String id, Authentication authentication) {
    WorkOrderDto workOrder = workOrderApi.getWorkOrderById(id);
    accessPolicy.requireWorkOrderRead(
        currentUserService.requireCurrentUser(authentication),
        workOrder.customerId(),
        workOrder.technicianId());
    return ResponseEntity.ok(workOrder);
  }

  @GetMapping("/my")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(
      operationId = "getMyWorkOrders",
      summary = "Get work orders for the authenticated user")
  public ResponseEntity<List<WorkOrderDto>> getMyWorkOrders(Authentication authentication) {
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    if (accessPolicy.isTechnician(currentUser)) {
      return ResponseEntity.ok(workOrderApi.findByTechnician(currentUser.id()));
    }
    if (accessPolicy.isOperationalUser(currentUser)) {
      return ResponseEntity.ok(workOrderApi.findAllWorkOrders());
    }
    return ResponseEntity.ok(workOrderApi.findByCustomer(currentUser.id()));
  }

  @GetMapping("/appointment/{appointmentId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(
      operationId = "getWorkOrderByAppointment",
      summary = "Get the work order created from an appointment")
  public ResponseEntity<WorkOrderDto> getByAppointment(
      @PathVariable String appointmentId, Authentication authentication) {
    AppointmentDto appointment = appointmentApi.getAppointmentById(appointmentId);
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), appointment.customerId());
    return ResponseEntity.ok(
        workOrderApi
            .findByAppointmentId(appointmentId)
            .orElseThrow(
                () ->
                    new com.apu.asc.common.exception.ResourceNotFoundException(
                        "Work order for appointment", appointmentId)));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(
      operationId = "createWorkOrder",
      summary = "Open a work order from a booking or for a walk-in")
  public ResponseEntity<WorkOrderDto> createWorkOrder(
      @Valid @RequestBody WorkOrderDto workOrderDto, Authentication authentication) {
    currentUserService.requireCurrentUser(authentication);
    WorkOrderDto securedWorkOrder = buildWorkOrderForCreate(workOrderDto);
    WorkOrderDto created = workOrderApi.createWorkOrder(securedWorkOrder);
    return ResponseEntity.created(URI.create("/api/v1/work-orders/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(
      operationId = "updateWorkOrder",
      summary = "Update work-order assignment and intake details")
  public ResponseEntity<WorkOrderDto> updateWorkOrder(
      @PathVariable String id,
      @Valid @RequestBody WorkOrderDto workOrderDto,
      Authentication authentication) {
    currentUserService.requireCurrentUser(authentication);
    WorkOrderDto existing = workOrderApi.getWorkOrderById(id);
    WorkOrderDto securedWorkOrder =
        new WorkOrderDto(
            existing.id(),
            existing.appointmentId(),
            existing.customerId(),
            existing.vehicleId(),
            existing.serviceId(),
            requireActiveTechnician(workOrderDto.technicianId()),
            existing.status(),
            workOrderDto.intakeNotes(),
            workOrderDto.diagnosticNotes(),
            existing.openedAt(),
            existing.startedAt(),
            existing.completedAt(),
            existing.createdAt(),
            existing.updatedAt());
    return ResponseEntity.ok(workOrderApi.updateWorkOrder(id, securedWorkOrder));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(operationId = "updateWorkOrderStatus", summary = "Update work-order execution status")
  public ResponseEntity<WorkOrderDto> updateStatus(
      @PathVariable String id,
      @RequestParam WorkOrderStatus status,
      Authentication authentication) {
    WorkOrderDto workOrder = workOrderApi.getWorkOrderById(id);
    accessPolicy.requireAssignedTechnicianOrOperational(
        currentUserService.requireCurrentUser(authentication), workOrder.technicianId());
    return ResponseEntity.ok(workOrderApi.updateStatus(id, status.name()));
  }

  @PatchMapping("/{id}/diagnostic-notes")
  @PreAuthorize("hasAnyRole('TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(
      operationId = "updateWorkOrderDiagnosticNotes",
      summary = "Update technician diagnostic notes")
  public ResponseEntity<WorkOrderDto> updateDiagnosticNotes(
      @PathVariable String id, @RequestBody String diagnosticNotes, Authentication authentication) {
    WorkOrderDto workOrder = workOrderApi.getWorkOrderById(id);
    accessPolicy.requireAssignedTechnicianOrOperational(
        currentUserService.requireCurrentUser(authentication), workOrder.technicianId());
    return ResponseEntity.ok(workOrderApi.updateDiagnosticNotes(id, diagnosticNotes));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('MANAGER')")
  @Operation(operationId = "deleteWorkOrder", summary = "Delete a work order")
  public ResponseEntity<Void> deleteWorkOrder(@PathVariable String id) {
    workOrderApi.deleteWorkOrder(id);
    return ResponseEntity.noContent().build();
  }

  private WorkOrderDto buildWorkOrderForCreate(WorkOrderDto requested) {
    if (requested.appointmentId() != null && !requested.appointmentId().isBlank()) {
      AppointmentDto appointment = appointmentApi.getAppointmentById(requested.appointmentId());
      if (!"CONFIRMED".equals(appointment.status())) {
        throw new IllegalArgumentException(
            "A work order can only be opened for a confirmed appointment.");
      }
      return new WorkOrderDto(
          null,
          appointment.id(),
          appointment.customerId(),
          appointment.vehicleId(),
          appointment.serviceId(),
          requireActiveTechnician(requested.technicianId()),
          "OPEN",
          requested.intakeNotes() != null ? requested.intakeNotes() : appointment.notes(),
          null,
          null,
          null,
          null,
          null,
          null);
    }

    if (isBlank(requested.customerId())
        || isBlank(requested.vehicleId())
        || isBlank(requested.serviceId())) {
      throw new IllegalArgumentException(
          "Walk-in work orders require a customer, vehicle, and service package.");
    }
    VehicleDto vehicle = vehicleApi.getVehicleById(requested.vehicleId());
    if (!requested.customerId().equals(vehicle.customerId())) {
      throw new IllegalArgumentException("The selected vehicle does not belong to the customer.");
    }
    return new WorkOrderDto(
        null,
        null,
        requested.customerId(),
        requested.vehicleId(),
        requested.serviceId(),
        requireActiveTechnician(requested.technicianId()),
        "OPEN",
        requested.intakeNotes(),
        null,
        null,
        null,
        null,
        null,
        null);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private String requireActiveTechnician(String technicianId) {
    if (isBlank(technicianId)) {
      return null;
    }
    UserDto technician = userApi.getUserById(technicianId);
    if (!"TECHNICIAN".equalsIgnoreCase(technician.role())
        || !"ACTIVE".equalsIgnoreCase(technician.status())) {
      throw new IllegalArgumentException(
          "Work orders can only be assigned to an active technician account.");
    }
    return technician.id();
  }
}
