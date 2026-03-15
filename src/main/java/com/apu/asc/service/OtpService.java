package com.apu.asc.service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
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

public class OtpService {

  private static final Map<String, OtpEntry> store = new HashMap<>();

  public String generateOtp(String username) {
    String code = String.format("%06d", new Random().nextInt(1_000_000));
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

  public void sendOtpEmail(String toEmail, String username, String otp) throws Exception {
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
    msg.setSubject("APU-ASC Password Reset OTP");

    String body =
        "Dear "
            + username
            + ",\n\n"
            + "Your one-time password (OTP) for resetting your APU-ASC account password is:\n\n"
            + "    "
            + otp
            + "\n\n"
            + "This code expires in 5 minutes. Do not share it with anyone.\n\n"
            + "If you did not request this, please ignore this email.\n\n"
            + "APU Automotive Service Centre";

    msg.setText(body);
    Transport.send(msg);
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
