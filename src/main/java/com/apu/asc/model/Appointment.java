package com.apu.asc.model;

import java.time.LocalDateTime;

public class Appointment {

  private final String appointmentId;
  private final String customerId;
  private final String counterStaffId;
  private String technicianId;
  private final String serviceId;
  private final String vehiclePlate;
  private final LocalDateTime appointmentDateTime;
  private ApptStatus status;
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
