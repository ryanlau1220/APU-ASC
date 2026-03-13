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
import com.apu.asc.util.Result;
import com.apu.asc.util.SessionManager;
import com.apu.asc.util.SystemLogger;
import com.apu.asc.util.ValidationUtil;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.UUID;

public class PaymentService {

  private final PaymentDAO paymentDAO;
  private final AppointmentDAO appointmentDAO;
  private final ServiceDAO serviceDAO;
  private final UserDAO userDAO;

  public PaymentService() {
    this.paymentDAO = PaymentDAO.getInstance();
    this.appointmentDAO = AppointmentDAO.getInstance();
    this.serviceDAO = ServiceDAO.getInstance();
    this.userDAO = UserDAO.getInstance();
  }

  public Result<Payment> processPayment(String appointmentId) {
    Appointment appt = appointmentDAO.findById(appointmentId);
    if (appt == null || appt.getStatus() != ApptStatus.COMPLETED)
      return Result.failure("Payment can only be processed for completed appointments.");
    if (paymentDAO.findByAppointment(appointmentId) != null)
      return Result.failure("Payment has already been recorded for this appointment.");

    Service service = serviceDAO.findById(appt.getServiceId());
    if (service == null)
      return Result.failure("Service record not found. Please contact the system administrator.");

    String id = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    Payment payment =
        new Payment(id, appointmentId, service.getPrice(), LocalDateTime.now(), false);
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

  public boolean sendReceipt(String paymentId) {
    Payment payment = paymentDAO.findByAppointment(paymentId);
    if (payment == null) {
      payment = findPaymentById(paymentId);
    }
    if (payment == null || payment.isReceiptSent()) return false;

    Appointment appt = appointmentDAO.findById(payment.getAppointmentId());
    if (appt == null) return false;

    User customer = userDAO.findById(appt.getCustomerId());
    if (customer == null) return false;

    Service service = serviceDAO.findById(appt.getServiceId());

    try {
      Path pdfPath = generateReceiptPdf(payment, appt, customer, service);
      sendEmail(customer.getEmail(), payment, pdfPath);
      payment.markReceiptSent();
      paymentDAO.save(payment);
      Files.deleteIfExists(pdfPath);
      SystemLogger.log(
          actorId(),
          "SEND_RECEIPT",
          payment.getPaymentId(),
          "Receipt emailed to " + customer.getEmail() + ".");
      return true;
    } catch (Exception e) {
      System.err.println("Receipt send failed: " + e.getMessage());
      return false;
    }
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

  private Path generateReceiptPdf(Payment payment, Appointment appt, User customer, Service service)
      throws Exception {
    Path path = Path.of(System.getProperty("java.io.tmpdir"), payment.getPaymentId() + ".pdf");

    Document doc = new Document();
    PdfWriter.getInstance(doc, new FileOutputStream(path.toFile()));
    doc.open();

    Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    Font bodyFont = new Font(Font.FontFamily.HELVETICA, 11);

    doc.add(new Paragraph("APU Automotive Service Centre", titleFont));
    doc.add(new Paragraph("Official Receipt", bodyFont));
    doc.add(Chunk.NEWLINE);

    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    addRow(table, "Receipt No", payment.getPaymentId());
    addRow(
        table,
        "Date",
        payment.getPaymentDateTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")));
    addRow(table, "Customer", customer.getFullName());
    addRow(table, "Vehicle Plate", appt.getVehiclePlate());
    addRow(table, "Service", service != null ? service.getServiceName() : appt.getServiceId());
    addRow(table, "Amount Paid", String.format("RM %.2f", payment.getAmountPaid()));

    doc.add(table);
    doc.add(Chunk.NEWLINE);
    doc.add(new Paragraph("Thank you for choosing APU-ASC!", bodyFont));
    doc.close();

    return path;
  }

  private void addRow(PdfPTable table, String label, String value) {
    table.addCell(new PdfPCell(new Phrase(label)));
    table.addCell(new PdfPCell(new Phrase(value)));
  }

  private void sendEmail(String toEmail, Payment payment, Path pdfPath) throws Exception {
    Properties mailProps = loadMailConfig();
    String from = mailProps.getProperty("mail.from");
    String password = mailProps.getProperty("mail.password");

    Session session =
        Session.getInstance(
            mailProps,
            new Authenticator() {
              @Override
              protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
              }
            });

    MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(from));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
    msg.setSubject("Your APU-ASC Service Receipt - " + payment.getPaymentId());

    MimeBodyPart textPart = new MimeBodyPart();
    textPart.setText(
        "Dear Customer,\n\nPlease find your service receipt attached.\n\nThank you,\nAPU-ASC");

    MimeBodyPart attachPart = new MimeBodyPart();
    attachPart.attachFile(pdfPath.toFile());

    Multipart multipart = new MimeMultipart();
    multipart.addBodyPart(textPart);
    multipart.addBodyPart(attachPart);
    msg.setContent(multipart);

    Transport.send(msg);
  }

  private Properties loadMailConfig() throws IOException {
    Properties props = new Properties();
    try (InputStream in = new FileInputStream("src/main/resources/config.properties")) {
      props.load(in);
    }
    return props;
  }

  private String actorId() {
    User u = SessionManager.getCurrentUser();
    return u != null ? u.getId() : "SYSTEM";
  }
}
