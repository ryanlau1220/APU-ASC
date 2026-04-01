package com.apu.asc.dao;

import com.apu.asc.model.Feedback;
import java.util.List;

public interface IFeedbackDAO {
  public void reload();
  public void save(Feedback feedback);
  public Feedback findByAppointment(String appointmentId);
  public List<Feedback> findByTechnician(String technicianId, AppointmentDAO appointmentDAO);
  public List<Feedback> getAll();
}
