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
