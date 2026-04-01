package com.apu.asc.service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import com.apu.asc.util.I18n;

public class OtpService implements IOtpService {

  private static final Map<String, OtpEntry> store = new HashMap<>();
  private static final Random RANDOM = new Random();
  private static final String NOTIFICATION_LOG = "data/notifications.txt";

  @Override
  public String generateOtp(String username) {
    String code = String.format("%06d", RANDOM.nextInt(1_000_000));
    store.put(username, new OtpEntry(code, LocalDateTime.now().plusMinutes(5)));
    return code;
  }

  @Override
  public boolean validateOtp(String username, String code) {
    OtpEntry entry = store.get(username);
    if (entry == null) return false;
    if (LocalDateTime.now().isAfter(entry.expiry)) {
      store.remove(username);
      return false;
    }
    return entry.code.equals(code);
  }

  @Override
  public void clearOtp(String username) {
    store.remove(username);
  }

  @Override
  public CompletableFuture<Void> sendOtpEmail(String toEmail, String username, String otp) {
    return CompletableFuture.runAsync(
        () -> {
          try (FileWriter fw = new FileWriter(NOTIFICATION_LOG, true);
               PrintWriter pw = new PrintWriter(fw)) {
            
            String subject = I18n.t("email.otp.subject");
            String body = I18n.format("email.otp.body", username, otp);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            String logEntry = String.format("[%s] TO: %s | SUBJECT: %s | BODY: %s", 
                                          timestamp, toEmail, subject, body.replace("\n", " "));
                                          
            pw.println(logEntry);
            System.out.println("OTP Simulated Email Sent: " + logEntry);
            
          } catch (IOException e) {
            throw new RuntimeException("Failed to log OTP email: " + e.getMessage(), e);
          }
        });
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
