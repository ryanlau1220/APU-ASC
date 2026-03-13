package com.apu.asc.service;

import com.apu.asc.dao.AppointmentDAO;
import com.apu.asc.dao.FeedbackDAO;
import com.apu.asc.dao.PaymentDAO;
import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Feedback;
import com.apu.asc.model.Payment;
import com.apu.asc.model.Role;
import com.apu.asc.model.User;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {

    private final AppointmentDAO appointmentDAO;
    private final PaymentDAO paymentDAO;
    private final FeedbackDAO feedbackDAO;
    private final UserDAO userDAO;
    private final ServiceDAO serviceDAO;

    public ReportService() {
        this.appointmentDAO = AppointmentDAO.getInstance();
        this.paymentDAO     = PaymentDAO.getInstance();
        this.feedbackDAO    = FeedbackDAO.getInstance();
        this.userDAO        = UserDAO.getInstance();
        this.serviceDAO     = ServiceDAO.getInstance();
    }

    public Map<String, Double> revenueByMonth() {
        Map<String, Double> result = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");

        paymentDAO.getAll().stream()
                .sorted((a, b) -> a.getPaymentDateTime().compareTo(b.getPaymentDateTime()))
                .forEach(p -> {
                    String key = p.getPaymentDateTime().format(fmt);
                    result.merge(key, p.getAmountPaid(), Double::sum);
                });
        return result;
    }

    public List<TechnicianStats> technicianPerformance() {
        List<User> technicians = userDAO.getAllByRole(Role.TECHNICIAN);
        List<TechnicianStats> stats = new ArrayList<>();

        for (User tech : technicians) {
            List<Appointment> completed = appointmentDAO.findByTechnician(tech.getId())
                    .stream()
                    .filter(a -> a.getStatus() == ApptStatus.COMPLETED)
                    .collect(Collectors.toList());

            List<Feedback> feedbacks = feedbackDAO.findByTechnician(tech.getId(), appointmentDAO);

            double avgRating = feedbacks.stream()
                    .mapToInt(Feedback::getRating)
                    .average()
                    .orElse(0.0);

            stats.add(new TechnicianStats(tech, completed.size(), avgRating));
        }
        return stats;
    }

    public static class TechnicianStats {
        public final User technician;
        public final int jobsCompleted;
        public final double averageRating;

        public TechnicianStats(User technician, int jobsCompleted, double averageRating) {
            this.technician    = technician;
            this.jobsCompleted = jobsCompleted;
            this.averageRating = averageRating;
        }
    }
}
