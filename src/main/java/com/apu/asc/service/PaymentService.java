package com.apu.asc.service;

import com.apu.asc.dao.AppointmentDAO;
import com.apu.asc.dao.PaymentDAO;
import com.apu.asc.dao.ServiceDAO;
import com.apu.asc.dao.UserDAO;
import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import com.apu.asc.model.Payment;
import com.apu.asc.model.Service;
import com.apu.asc.model.User;
import com.apu.asc.util.I18n;
import com.apu.asc.util.Result;
import com.apu.asc.util.SessionManager;
import com.apu.asc.util.SystemLogger;
import com.apu.asc.util.ValidationUtil;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PaymentService implements IPaymentService {

  private final PaymentDAO paymentDAO;
  private final AppointmentDAO appointmentDAO;
  private final ServiceDAO serviceDAO;
  private final UserDAO userDAO;
  private static final String NOTIFICATION_LOG = "data/notifications.txt";

  public PaymentService() {
    this.paymentDAO = PaymentDAO.getInstance();
    this.appointmentDAO = AppointmentDAO.getInstance();
    this.serviceDAO = ServiceDAO.getInstance();
    this.userDAO = UserDAO.getInstance();
  }

  public Result<Payment> processPayment(String appointmentId) {
    Appointment appt = appointmentDAO.findById(appointmentId);
    if (appt == null || appt.getStatus() != ApptStatus.COMPLETED)
      return Result.failure("err.payment.completedOnly");
    if (paymentDAO.findByAppointment(appointmentId) != null)
      return Result.failure("err.payment.alreadyRecorded");

    Service service = serviceDAO.findById(appt.getServiceId());
    if (service == null) return Result.failure("err.payment.serviceMissing");

    String id = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    double originalPrice = service.getPrice();
    double historicalSpend =
        appointmentDAO.findByCustomer(appt.getCustomerId()).stream()
            .map(a -> paymentDAO.findByAppointment(a.getAppointmentId()))
            .filter(p -> p != null)
            .mapToDouble(Payment::getAmountPaid)
            .sum();
    double discountRate;
    if (historicalSpend > 5000) discountRate = 0.10;
    else if (historicalSpend > 1000) discountRate = 0.05;
    else discountRate = 0.0;
    double discountAmount = Math.round(originalPrice * discountRate * 100.0) / 100.0;
    double finalAmount = Math.round((originalPrice - discountAmount) * 100.0) / 100.0;

    Payment payment =
        new Payment(id, appointmentId, finalAmount, discountAmount, LocalDateTime.now(), false);
    String paymentViolations = ValidationUtil.getViolations(payment);
    if (paymentViolations != null) return Result.failure(paymentViolations);
    paymentDAO.save(payment);
    SystemLogger.log(
        actorId(),
        "PROCESS_PAYMENT",
        id,
        "Payment " + id + " recorded for appointment " + appointmentId + ".");
    return Result.success(payment);
  }

  public CompletableFuture<Boolean> sendReceipt(String paymentId) {
    return CompletableFuture.supplyAsync(
        () -> {
          Payment payment = findPaymentById(paymentId);
          if (payment == null || payment.isReceiptSent()) return false;

          Appointment appt = appointmentDAO.findById(payment.getAppointmentId());
          if (appt == null) return false;

          User customer = userDAO.findById(appt.getCustomerId());
          if (customer == null) return false;

          Service service = serviceDAO.findById(appt.getServiceId());

          try {
            Path txtPath = generateReceiptTxt(payment, appt, customer, service);
            logReceiptEmail(customer.getEmail(), payment, txtPath);
            payment.markReceiptSent();
            paymentDAO.save(payment);
            SystemLogger.log(
                actorId(),
                "SEND_RECEIPT",
                payment.getPaymentId(),
                "Receipt generated and logged for " + customer.getEmail() + ".");
            return true;
          } catch (Exception e) {
            System.err.println("Receipt send failed: " + e.getMessage());
            return false;
          }
        });
  }

  public Payment findByAppointment(String appointmentId) {
    return paymentDAO.findByAppointment(appointmentId);
  }

  public java.util.List<Payment> getAll() {
    return paymentDAO.getAll();
  }

  private Payment findPaymentById(String paymentId) {
    return paymentDAO.getAll().stream()
        .filter(p -> p.getPaymentId().equals(paymentId))
        .findFirst()
        .orElse(null);
  }

  private Path generateReceiptTxt(Payment payment, Appointment appt, User customer, Service service)
      throws Exception {
    Path path = Path.of(System.getProperty("java.io.tmpdir"), payment.getPaymentId() + "_receipt.txt");

    try (PrintWriter pw = new PrintWriter(new FileWriter(path.toFile()))) {
      pw.println("=========================================");
      pw.println("               " + I18n.t("receipt.title"));
      pw.println("          " + I18n.t("receipt.subtitle"));
      pw.println("=========================================");
      pw.println();
      
      pw.printf("%-20s: %s%n", I18n.t("receipt.label.no"), payment.getPaymentId());
      pw.printf("%-20s: %s%n", I18n.t("receipt.label.date"), 
          payment.getPaymentDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", I18n.getLocale())));
      pw.printf("%-20s: %s%n", I18n.t("receipt.label.customer"), customer.getFullName());
      pw.printf("%-20s: %s%n", I18n.t("receipt.label.vehiclePlate"), appt.getVehiclePlate());
      pw.printf("%-20s: %s%n", I18n.t("receipt.label.service"), 
          service != null ? service.getServiceName() : appt.getServiceId());
          
      pw.println();
      
      double originalPrice = payment.getAmountPaid() + payment.getDiscountAmount();
      pw.printf("%-20s: RM %.2f%n", I18n.t("receipt.label.originalPrice"), originalPrice);
      
      if (payment.getDiscountAmount() > 0) {
        pw.printf("%-20s: -RM %.2f%n", I18n.t("receipt.label.discount"), payment.getDiscountAmount());
      }
      
      pw.printf("%-20s: RM %.2f%n", I18n.t("receipt.label.amountPaid"), payment.getAmountPaid());
      
      pw.println();
      pw.println("=========================================");
      pw.println("        " + I18n.t("receipt.thanks"));
      pw.println("=========================================");
    }

    return path;
  }

  private void logReceiptEmail(String toEmail, Payment payment, Path txtPath) throws Exception {
    try (FileWriter fw = new FileWriter(NOTIFICATION_LOG, true);
         PrintWriter pw = new PrintWriter(fw)) {
      
      String subject = I18n.format("email.receipt.subject", payment.getPaymentId());
      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      String receiptContent = Files.readString(txtPath).replace("\n", " | ");
      
      String logEntry = String.format("[%s] TO: %s | SUBJECT: %s | BODY: %s | ATTACHMENT: %s", 
                                    timestamp, toEmail, subject, I18n.t("email.receipt.body"), receiptContent);
                                    
      pw.println(logEntry);
      System.out.println("Receipt Simulated Email Sent: " + logEntry);
    }
  }

  private String actorId() {
    User u = SessionManager.getCurrentUser();
    return u != null ? u.getId() : "SYSTEM";
  }
}
