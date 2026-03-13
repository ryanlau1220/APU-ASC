package com.apu.asc.service;

import com.apu.asc.dao.AppointmentDAO;
import com.apu.asc.dao.FeedbackDAO;
import com.apu.asc.dao.PaymentDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Feedback;
import com.apu.asc.model.User;
import com.apu.asc.util.DataSanitizer;
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

  public Feedback submitFeedback(String appointmentId, int rating, String comments) {
    Appointment appt = appointmentDAO.findById(appointmentId);
    if (appt == null || appt.getStatus() != ApptStatus.COMPLETED) return null;
    if (paymentDAO.findByAppointment(appointmentId) == null) return null;
    if (feedbackDAO.findByAppointment(appointmentId) != null) return null;

    if (rating < 1 || rating > 5) return null;

    String id = "FBK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    Feedback feedback = new Feedback(id, appointmentId, rating, DataSanitizer.clean(comments));

    if (ValidationUtil.getViolations(feedback) != null) return null;

    feedbackDAO.save(feedback);
    SystemLogger.log(
        actorId(),
        "SUBMIT_FEEDBACK",
        id,
        "Feedback submitted for appointment " + appointmentId + " — rating: " + rating + ".");
    return feedback;
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
