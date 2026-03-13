package com.apu.asc.model;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class Appointment {

  @NotBlank(message = "Appointment ID must not be blank")
  private final String appointmentId;

  @NotBlank(message = "Customer ID must not be blank")
  private final String customerId;

  @NotBlank(message = "Counter staff ID must not be blank")
  private final String counterStaffId;

  @NotBlank(message = "Technician ID must not be blank")
  private String technicianId;

  @NotBlank(message = "Service ID must not be blank")
  private final String serviceId;

  @NotBlank(message = "Vehicle plate must not be blank")
  private final String vehiclePlate;

  @NotNull(message = "Appointment date and time must be specified")
  @FutureOrPresent(message = "Cannot book an appointment in the past")
  private final LocalDateTime appointmentDateTime;

  @NotNull(message = "Appointment status must not be null")
  private ApptStatus status;

  @NotNull(message = "Technician notes must not be null")
  private String technicianNotes;

  public Appointment(
      String appointmentId,
      String customerId,
      String counterStaffId,
      String technicianId,
      String serviceId,
      String vehiclePlate,
      LocalDateTime appointmentDateTime,
      ApptStatus status,
      String technicianNotes) {
    this.appointmentId = appointmentId;
    this.customerId = customerId;
    this.counterStaffId = counterStaffId;
    this.technicianId = technicianId;
    this.serviceId = serviceId;
    this.vehiclePlate = vehiclePlate;
    this.appointmentDateTime = appointmentDateTime;
    this.status = status;
    this.technicianNotes = technicianNotes;
  }

  public String getAppointmentId() {
    return appointmentId;
  }

  public String getCustomerId() {
    return customerId;
  }

  public String getCounterStaffId() {
    return counterStaffId;
  }

  public String getTechnicianId() {
    return technicianId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public String getVehiclePlate() {
    return vehiclePlate;
  }

  public LocalDateTime getAppointmentDateTime() {
    return appointmentDateTime;
  }

  public ApptStatus getStatus() {
    return status;
  }

  public String getTechnicianNotes() {
    return technicianNotes;
  }

  public void setTechnicianNotes(String notes) {
    this.technicianNotes = notes;
  }

  public void markAsAssigned(String technicianId) {
    this.technicianId = technicianId;
    this.status = ApptStatus.ASSIGNED;
  }

  public void markAsCompleted() {
    this.status = ApptStatus.COMPLETED;
  }

  public LocalDateTime getEndDateTime(int durationHours) {
    return appointmentDateTime.plusHours(durationHours);
  }

  public String toFileString() {
    return String.join(
        "||",
        appointmentId,
        customerId,
        counterStaffId,
        technicianId,
        serviceId,
        vehiclePlate,
        appointmentDateTime.toString(),
        status.name(),
        technicianNotes);
  }
}
