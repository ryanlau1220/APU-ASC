package com.apu.asc.service;

import com.apu.asc.util.I18n;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public class OtpService {

  private static final Map<String, OtpEntry> store = new HashMap<>();
  private static final Random RANDOM = new Random();

  public String generateOtp(String username) {
    String code = String.format("%06d", RANDOM.nextInt(1_000_000));
    store.put(username, new OtpEntry(code, LocalDateTime.now().plusMinutes(5)));
    return code;
  }

  public boolean validateOtp(String username, String code) {
    OtpEntry entry = store.get(username);
    if (entry == null) return false;
    if (LocalDateTime.now().isAfter(entry.expiry)) {
      store.remove(username);
      return false;
    }
    return entry.code.equals(code);
  }

  public void clearOtp(String username) {
    store.remove(username);
  }

  public CompletableFuture<Void> sendOtpEmail(String toEmail, String username, String otp) {
    return CompletableFuture.runAsync(
        () -> {
          try {
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
            msg.setSubject(I18n.t("email.otp.subject"));

            String body = I18n.format("email.otp.body", username, otp);

            msg.setText(body);
            Transport.send(msg);
          } catch (MessagingException | IOException e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
          }
        });
  }

  private Properties loadMailConfig() throws IOException {
    Properties props = new Properties();
    try (InputStream in = new FileInputStream("src/main/resources/config.properties")) {
      props.load(in);
    }
    return props;
  }

  private static class OtpEntry {
    final String code;
    final LocalDateTime expiry;

    OtpEntry(String code, LocalDateTime expiry) {
      this.code = code;
      this.expiry = expiry;
    }
  }
}
