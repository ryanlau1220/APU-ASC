package com.apu.asc.appointment.internal;

import com.apu.asc.appointment.AppointmentApi;
import com.apu.asc.appointment.AppointmentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
@Tag(name = "Appointments", description = "Booking and scheduling management APIs")
class AppointmentController {

  private final AppointmentApi appointmentApi;

  @GetMapping
  @PreAuthorize("hasAnyRole('STAFF', 'MANAGER')")
  @Operation(summary = "Get all appointments")
  public ResponseEntity<List<AppointmentDto>> getAllAppointments() {
    return ResponseEntity.ok(appointmentApi.findAllAppointments());
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
      @RequestBody final AppointmentDto appointmentDto) {
    return ResponseEntity.ok(appointmentApi.createAppointment(appointmentDto));
  }
}
