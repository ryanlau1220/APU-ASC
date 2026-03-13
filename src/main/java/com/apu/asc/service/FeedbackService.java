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

public class FeedbackService {

  private final FeedbackDAO feedbackDAO;
  private final AppointmentDAO appointmentDAO;
  private final PaymentDAO paymentDAO;

  public FeedbackService() {
    this.feedbackDAO = FeedbackDAO.getInstance();
    this.appointmentDAO = AppointmentDAO.getInstance();
    this.paymentDAO = PaymentDAO.getInstance();
  }

  public Result<Feedback> submitFeedback(String appointmentId, int rating, String comments) {
    Appointment appt = appointmentDAO.findById(appointmentId);
    if (appt == null || appt.getStatus() != ApptStatus.COMPLETED)
      return Result.failure("Feedback can only be submitted for completed appointments.");
    if (paymentDAO.findByAppointment(appointmentId) == null)
      return Result.failure("Payment must be completed before submitting feedback.");
    if (feedbackDAO.findByAppointment(appointmentId) != null)
      return Result.failure("Feedback has already been submitted for this appointment.");

    if (rating < 1 || rating > 5) return Result.failure("Rating must be between 1 and 5.");

    String id = "FBK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    Feedback feedback = new Feedback(id, appointmentId, rating, DataSanitizer.clean(comments));

    String violations = ValidationUtil.getViolations(feedback);
    if (violations != null) return Result.failure(violations);

    feedbackDAO.save(feedback);
    SystemLogger.log(
        actorId(),
        "SUBMIT_FEEDBACK",
        id,
        "Feedback submitted for appointment " + appointmentId + " — rating: " + rating + ".");
    return Result.success(feedback);
  }

  public Feedback findByAppointment(String appointmentId) {
    return feedbackDAO.findByAppointment(appointmentId);
  }

  public List<Feedback> getByTechnician(String technicianId) {
    return feedbackDAO.findByTechnician(technicianId, appointmentDAO);
  }

  public List<Feedback> getAll() {
    return feedbackDAO.getAll();
  }

  private String actorId() {
    User u = SessionManager.getCurrentUser();
    return u != null ? u.getId() : "SYSTEM";
  }
}
