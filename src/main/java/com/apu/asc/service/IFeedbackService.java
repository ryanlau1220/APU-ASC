package com.apu.asc.service;

import com.apu.asc.dao.AppointmentDAO;
import com.apu.asc.dao.FeedbackDAO;
import com.apu.asc.dao.PaymentDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Feedback;
import com.apu.asc.model.User;
import com.apu.asc.util.DataSanitizer;
import com.apu.asc.util.Result;
import com.apu.asc.util.SessionManager;
import com.apu.asc.util.SystemLogger;
import com.apu.asc.util.ValidationUtil;
import java.util.List;
import java.util.UUID;

public interface IFeedbackService {
  public Result<Feedback> submitFeedback(String appointmentId, int rating, String comments);
  public Feedback findByAppointment(String appointmentId);
  public List<Feedback> getByTechnician(String technicianId);
  public List<Feedback> getAll();
}
