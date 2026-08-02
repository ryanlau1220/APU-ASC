package com.apu.asc.appointment.internal;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
import com.apu.asc.user.UserApi;
import com.apu.asc.user.UserDto;
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
  private final UserApi userApi;

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
  public ResponseEntity<AppointmentDto> getAppointmentById(@PathVariable final String id) {
    return ResponseEntity.ok(appointmentApi.getAppointmentById(id));
  }

  @GetMapping("/my")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(
      summary = "Get my appointments",
      description = "Retrieves appointments for logged-in user")
  public ResponseEntity<List<AppointmentDto>> getMyAppointments(Authentication authentication) {
    String currentUserId = resolveUserId(authentication);
    if (currentUserId == null) {
      return ResponseEntity.ok(List.of());
    }
    return ResponseEntity.ok(appointmentApi.findByCustomer(currentUserId));
  }

  @GetMapping("/customer/{customerId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get appointments by customer ID")
  public ResponseEntity<List<AppointmentDto>> getCustomerAppointments(
      @PathVariable final String customerId) {
    return ResponseEntity.ok(appointmentApi.findByCustomer(customerId));
  }

  @GetMapping("/technician/{technicianId}")
  @PreAuthorize("hasAnyRole('TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(summary = "Get assigned appointments for technician")
  public ResponseEntity<List<AppointmentDto>> getTechnicianAppointments(
      @PathVariable final String technicianId) {
    return ResponseEntity.ok(appointmentApi.findByTechnician(technicianId));
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF')")
  @Operation(summary = "Book new appointment")
  public ResponseEntity<AppointmentDto> createAppointment(
      @Valid @RequestBody final AppointmentDto appointmentDto) {
    AppointmentDto created = appointmentApi.createAppointment(appointmentDto);
    return ResponseEntity.created(URI.create("/api/v1/appointments/" + created.id())).body(created);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'MANAGER')")
  @Operation(summary = "Update appointment details")
  public ResponseEntity<AppointmentDto> updateAppointment(
      @PathVariable final String id, @Valid @RequestBody final AppointmentDto appointmentDto) {
    return ResponseEntity.ok(appointmentApi.updateAppointment(id, appointmentDto));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('TECHNICIAN', 'STAFF', 'MANAGER')")
  @Operation(summary = "Update appointment status", operationId = "updateAppointmentStatus")
  public ResponseEntity<AppointmentDto> updateStatus(
      @PathVariable final String id, @RequestParam final String status) {
    return ResponseEntity.ok(appointmentApi.updateStatus(id, status));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(summary = "Cancel/delete appointment")
  public ResponseEntity<Void> deleteAppointment(@PathVariable final String id) {
    appointmentApi.deleteAppointment(id);
    return ResponseEntity.noContent().build();
  }

  private String resolveUserId(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return null;
    }
    String name = authentication.getName();
    return userApi
        .findByUsername(name)
        .or(() -> userApi.findByEmail(name))
        .or(() -> userApi.findByKeycloakId(name))
        .map(UserDto::id)
        .orElse(name);
  }
}
