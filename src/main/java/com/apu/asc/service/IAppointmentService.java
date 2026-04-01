package com.apu.asc.service;

import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import java.util.List;

public interface IAppointmentService {
  public boolean assignTechnician(String appointmentId, String technicianId);
  public boolean updateNotes(String appointmentId, String technicianNotes);
  public boolean completeAppointment(String appointmentId, String technicianNotes);
  public List<Appointment> getByCustomer(String customerId);
  public List<Appointment> getByTechnician(String technicianId);
  public List<Appointment> getByStatus(ApptStatus status);
  public Appointment findById(String id);
  public List<Appointment> getAll();
  public boolean hasTechnicianConflict(String technicianId, Appointment appt);
}
