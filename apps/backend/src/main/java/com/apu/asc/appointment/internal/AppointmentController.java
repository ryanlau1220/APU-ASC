package com.apu.asc.appointment.internal;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
import com.apu.asc.common.security.AccessPolicy;
import com.apu.asc.common.security.AuthenticatedUser;
import com.apu.asc.user.CurrentUserService;
import com.apu.asc.vehicle.VehicleApi;
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
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Booking and scheduling management APIs")
class AppointmentController {

  private final AppointmentApi appointmentApi;
  private final VehicleApi vehicleApi;
  private final CurrentUserService currentUserService;
  private final AccessPolicy accessPolicy;

  @GetMapping
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(summary = "Get all appointments", description = "Retrieves a list of all appointments")
  public ResponseEntity<List<AppointmentDto>> getAllAppointments() {
    return ResponseEntity.ok(appointmentApi.findAllAppointments());
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(
      summary = "Get appointment by ID",
      description = "Retrieves specific appointment details")
  public ResponseEntity<AppointmentDto> getAppointmentById(
      @PathVariable final String id, Authentication authentication) {
    AppointmentDto appointment = appointmentApi.getAppointmentById(id);
    accessPolicy.requireAppointmentRead(
        currentUserService.requireCurrentUser(authentication),
        appointment.customerId(),
        appointment.technicianId());
    return ResponseEntity.ok(appointment);
  }

  @GetMapping("/my")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(
      summary = "Get my appointments",
      description = "Retrieves appointments for logged-in user")
  public ResponseEntity<List<AppointmentDto>> getMyAppointments(Authentication authentication) {
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    if (accessPolicy.isTechnician(currentUser)) {
      return ResponseEntity.ok(appointmentApi.findByTechnician(currentUser.id()));
    }
    return ResponseEntity.ok(appointmentApi.findByCustomer(currentUser.id()));
  }

  @GetMapping("/customer/{customerId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get appointments by customer ID")
  public ResponseEntity<List<AppointmentDto>> getCustomerAppointments(
      @PathVariable final String customerId, Authentication authentication) {
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), customerId);
    return ResponseEntity.ok(appointmentApi.findByCustomer(customerId));
  }

  @GetMapping("/technician/{technicianId}")
  @PreAuthorize("hasAnyRole('TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get assigned appointments for technician")
  public ResponseEntity<List<AppointmentDto>> getTechnicianAppointments(
      @PathVariable final String technicianId, Authentication authentication) {
    accessPolicy.requireSelfOrOperational(
        currentUserService.requireCurrentUser(authentication), technicianId);
    return ResponseEntity.ok(appointmentApi.findByTechnician(technicianId));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF')")
  @Operation(summary = "Book new appointment")
  public ResponseEntity<AppointmentDto> createAppointment(
      @Valid @RequestBody final AppointmentDto appointmentDto, Authentication authentication) {
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    AppointmentDto securedAppointment = appointmentDto;
    if (!accessPolicy.isOperationalUser(currentUser)) {
      String vehicleId = appointmentDto.vehicleId();
      if (vehicleId == null || vehicleId.isBlank()) {
        throw new IllegalArgumentException("A vehicle is required to book an appointment.");
      }
      accessPolicy.requireSelfOrOperational(
          currentUser, vehicleApi.getVehicleById(vehicleId).customerId());
      securedAppointment =
          new AppointmentDto(
              null,
              currentUser.id(),
              vehicleId,
              appointmentDto.serviceId(),
              null,
              appointmentDto.appointmentDate(),
              appointmentDto.timeSlot(),
              "PENDING",
              appointmentDto.notes(),
              null,
              null);
    }
    AppointmentDto created = appointmentApi.createAppointment(securedAppointment);
    return ResponseEntity.created(URI.create("/api/v1/appointments/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Update appointment details")
  public ResponseEntity<AppointmentDto> updateAppointment(
      @PathVariable final String id,
      @Valid @RequestBody final AppointmentDto appointmentDto,
      Authentication authentication) {
    AppointmentDto existing = appointmentApi.getAppointmentById(id);
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    AppointmentDto securedAppointment = appointmentDto;

    if (!accessPolicy.isOperationalUser(currentUser)) {
      accessPolicy.requireSelfOrOperational(currentUser, existing.customerId());
      String vehicleId =
          appointmentDto.vehicleId() != null ? appointmentDto.vehicleId() : existing.vehicleId();
      accessPolicy.requireSelfOrOperational(
          currentUser, vehicleApi.getVehicleById(vehicleId).customerId());
      securedAppointment =
          new AppointmentDto(
              existing.id(),
              existing.customerId(),
              vehicleId,
              appointmentDto.serviceId(),
              existing.technicianId(),
              appointmentDto.appointmentDate(),
              appointmentDto.timeSlot(),
              existing.status(),
              appointmentDto.notes(),
              existing.createdAt(),
              existing.updatedAt());
    }

    return ResponseEntity.ok(appointmentApi.updateAppointment(id, securedAppointment));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(summary = "Update appointment status", operationId = "updateAppointmentStatus")
  public ResponseEntity<AppointmentDto> updateStatus(
      @PathVariable final String id,
      @RequestParam final String status,
      Authentication authentication) {
    AppointmentDto appointment = appointmentApi.getAppointmentById(id);
    accessPolicy.requireAssignedTechnicianOrOperational(
        currentUserService.requireCurrentUser(authentication), appointment.technicianId());
    return ResponseEntity.ok(appointmentApi.updateStatus(id, status));
  }

  @PatchMapping("/{id}/cancel")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Cancel an appointment", operationId = "cancelAppointment")
  public ResponseEntity<AppointmentDto> cancelAppointment(
      @PathVariable final String id, Authentication authentication) {
    AppointmentDto appointment = appointmentApi.getAppointmentById(id);
    AuthenticatedUser currentUser = currentUserService.requireCurrentUser(authentication);
    accessPolicy.requireSelfOrOperational(currentUser, appointment.customerId());

    if (!accessPolicy.isOperationalUser(currentUser) && !"PENDING".equals(appointment.status())) {
      throw new IllegalArgumentException("Customers can cancel only pending appointments.");
    }

    return ResponseEntity.ok(appointmentApi.updateStatus(id, "CANCELLED"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(summary = "Cancel/delete appointment")
  public ResponseEntity<Void> deleteAppointment(@PathVariable final String id) {
    appointmentApi.deleteAppointment(id);
    return ResponseEntity.noContent().build();
  }
}
