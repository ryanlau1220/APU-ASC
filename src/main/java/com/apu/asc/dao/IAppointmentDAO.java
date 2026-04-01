package com.apu.asc.dao;

import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import java.util.List;

public interface IAppointmentDAO {
  public void reload();
  public void save(Appointment appointment);
  public Appointment findById(String id);
  public List<Appointment> findByCustomer(String customerId);
  public List<Appointment> findByTechnician(String technicianId);
  public List<Appointment> findByStatus(ApptStatus status);
  public List<Appointment> getAll();
}
