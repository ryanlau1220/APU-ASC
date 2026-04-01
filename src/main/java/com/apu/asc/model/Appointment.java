package com.apu.asc.model;

import com.apu.asc.util.validation.FutureOrPresent;
import com.apu.asc.util.validation.NotBlank;
import com.apu.asc.util.validation.NotNull;
import java.time.LocalDateTime;

public class Appointment {

  @NotBlank(message = "err.validation.apptIdRequired")
  private final String appointmentId;

  @NotBlank(message = "err.validation.customerIdRequired")
  private final String customerId;

  @NotBlank(message = "err.validation.staffIdRequired")
  private final String counterStaffId;

  @NotBlank(message = "err.validation.technicianIdRequired")
  private String technicianId;

  @NotBlank(message = "err.validation.serviceIdRequired")
  private final String serviceId;

  @NotBlank(message = "err.validation.vehiclePlateRequired")
  private final String vehiclePlate;

  @NotNull(message = "err.validation.apptDateTimeRequired")
  @FutureOrPresent(message = "Cannot book an appointment in the past")
  private final LocalDateTime appointmentDateTime;

  @NotNull(message = "err.validation.apptStatusRequired")
  private ApptStatus status;

  @NotNull(message = "err.validation.techNotesRequired")
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
