package com.apu.asc.service;

import com.apu.asc.dao.AppointmentDAO;
import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Service;
import com.apu.asc.model.ServiceType;
import com.apu.asc.util.DataSanitizer;
import com.apu.asc.util.Result;
import com.apu.asc.util.SessionManager;
import com.apu.asc.util.SystemLogger;
import com.apu.asc.util.ValidationUtil;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class AppointmentService {

  private static final int NORMAL_DURATION_HOURS = 1;
  private static final int MAJOR_DURATION_HOURS = 3;

  private final AppointmentDAO appointmentDAO;
  private final ServiceDAO serviceDAO;

  public AppointmentService() {
    this.appointmentDAO = AppointmentDAO.getInstance();
    this.serviceDAO = ServiceDAO.getInstance();
  }

  public Result<Appointment> createAppointment(
      String customerId,
      String counterStaffId,
      String serviceId,
      String vehiclePlate,
      LocalDateTime dateTime) {
    vehiclePlate = DataSanitizer.clean(vehiclePlate).trim().toUpperCase();

    Service service = serviceDAO.findById(serviceId);
    if (service == null || !service.isActive())
      return Result.failure("The selected service is unavailable or has been deactivated.");

    if (hasConflict(null, dateTime, service.getType()))
      return Result.failure(
          "The selected time slot conflicts with an existing appointment. Please choose a different time.");

    String id = "APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    Appointment appt =
        new Appointment(
            id,
            customerId,
            counterStaffId,
            "UNASSIGNED",
            serviceId,
            vehiclePlate,
            dateTime,
            ApptStatus.PENDING,
            "");
    String apptViolations = ValidationUtil.getViolations(appt);
    if (apptViolations != null) return Result.failure(apptViolations);
    appointmentDAO.save(appt);
    SystemLogger.log(
        actorId(),
        "CREATE_APPT",
        id,
        "Appointment created for customer " + customerId + " - " + vehiclePlate + ".");
    return Result.success(appt);
  }

  public boolean assignTechnician(String appointmentId, String technicianId) {
    Appointment appt = appointmentDAO.findById(appointmentId);
    if (appt == null || appt.getStatus() != ApptStatus.PENDING) return false;

    Service service = serviceDAO.findById(appt.getServiceId());
    if (service == null) return false;

    if (technicianHasConflict(
        technicianId, appointmentId, appt.getAppointmentDateTime(), service.getType()))
      return false;

    appt.markAsAssigned(technicianId);
    appointmentDAO.save(appt);
    SystemLogger.log(
        actorId(),
        "ASSIGN_TECHNICIAN",
        appointmentId,
        "Appointment " + appointmentId + " assigned to technician " + technicianId + ".");
    return true;
  }

  public boolean completeAppointment(String appointmentId, String technicianNotes) {
    Appointment appt = appointmentDAO.findById(appointmentId);
    if (appt == null || appt.getStatus() != ApptStatus.ASSIGNED) return false;

    appt.setTechnicianNotes(DataSanitizer.clean(technicianNotes));
    appt.markAsCompleted();
    appointmentDAO.save(appt);
    SystemLogger.log(
        actorId(),
        "COMPLETE_APPT",
        appointmentId,
        "Appointment " + appointmentId + " marked as COMPLETED.");
    return true;
  }

  public List<Appointment> getByCustomer(String customerId) {
    return appointmentDAO.findByCustomer(customerId);
  }

  public List<Appointment> getByTechnician(String technicianId) {
    return appointmentDAO.findByTechnician(technicianId);
  }

  public List<Appointment> getByStatus(ApptStatus status) {
    return appointmentDAO.findByStatus(status);
  }

  public Appointment findById(String id) {
    return appointmentDAO.findById(id);
  }

  public List<Appointment> getAll() {
    return appointmentDAO.getAll();
  }

  private boolean hasConflict(String excludeApptId, LocalDateTime dateTime, ServiceType type) {
    int duration = type == ServiceType.MAJOR ? MAJOR_DURATION_HOURS : NORMAL_DURATION_HOURS;
    LocalDateTime newEnd = dateTime.plusHours(duration);

    return appointmentDAO.getAll().stream()
        .filter(a -> !a.getAppointmentId().equals(excludeApptId))
        .filter(a -> a.getStatus() != ApptStatus.COMPLETED)
        .anyMatch(
            a -> {
              Service s = serviceDAO.findById(a.getServiceId());
              int existDur =
                  s != null && s.getType() == ServiceType.MAJOR
                      ? MAJOR_DURATION_HOURS
                      : NORMAL_DURATION_HOURS;
              LocalDateTime existEnd = a.getAppointmentDateTime().plusHours(existDur);
              return dateTime.isBefore(existEnd) && newEnd.isAfter(a.getAppointmentDateTime());
            });
  }

  private boolean technicianHasConflict(
      String technicianId, String excludeApptId, LocalDateTime dateTime, ServiceType type) {
    int duration = type == ServiceType.MAJOR ? MAJOR_DURATION_HOURS : NORMAL_DURATION_HOURS;
    LocalDateTime newEnd = dateTime.plusHours(duration);

    return appointmentDAO.findByTechnician(technicianId).stream()
        .filter(a -> !a.getAppointmentId().equals(excludeApptId))
        .filter(a -> a.getStatus() != ApptStatus.COMPLETED)
        .anyMatch(
            a -> {
              Service s = serviceDAO.findById(a.getServiceId());
              int existDur =
                  s != null && s.getType() == ServiceType.MAJOR
                      ? MAJOR_DURATION_HOURS
                      : NORMAL_DURATION_HOURS;
              LocalDateTime existEnd = a.getAppointmentDateTime().plusHours(existDur);
              return dateTime.isBefore(existEnd) && newEnd.isAfter(a.getAppointmentDateTime());
            });
  }

  private String actorId() {
    var u = SessionManager.getCurrentUser();
    return u != null ? u.getId() : "SYSTEM";
  }
}
