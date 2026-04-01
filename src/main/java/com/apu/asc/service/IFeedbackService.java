package com.apu.asc.service;

import com.apu.asc.model.Feedback;
import com.apu.asc.util.Result;
import java.util.List;

public interface IFeedbackService {
  public Result<Feedback> submitFeedback(String appointmentId, int rating, String comments);
  public Feedback findByAppointment(String appointmentId);
  public List<Feedback> getByTechnician(String technicianId);
  public List<Feedback> getAll();
}
