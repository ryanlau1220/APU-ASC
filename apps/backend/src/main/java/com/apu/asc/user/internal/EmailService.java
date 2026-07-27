package com.apu.asc.user.internal;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;

  @Value("${spring.mail.username:}")
  private String fromEmail;

  public void sendOtpEmail(String recipientEmail, String otpCode) {
    CompletableFuture.runAsync(
        () -> {
          try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String sender =
                StringUtils.hasText(fromEmail) ? fromEmail.trim() : "noreply@apu-asc.com";

            helper.setFrom(sender);
            helper.setTo(recipientEmail);
            helper.setSubject("APU-ASC Password Reset OTP Code");

            String htmlContent =
                """
                <div style="font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; background-color: #0b0f19; color: #f8fafc; border-radius: 12px; border: 1px solid #1e293b;">
                  <div style="text-align: center; margin-bottom: 20px;">
                    <h2 style="color: #0284c7; margin: 0;">APU-ASC Password Reset</h2>
                    <p style="color: #94a3b8; font-size: 14px;">Automotive Service Centre Verification</p>
                  </div>
                  <div style="background-color: #131c31; padding: 20px; border-radius: 8px; text-align: center; margin-bottom: 20px;">
                    <p style="color: #cbd5e1; font-size: 14px; margin-bottom: 10px;">Your 6-digit OTP Verification Code is:</p>
                    <div style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #38bdf8; padding: 10px; background-color: #1e293b; border-radius: 6px; display: inline-block;">
                      %s
                    </div>
                    <p style="color: #64748b; font-size: 12px; margin-top: 15px;">This code will expire in 10 minutes.</p>
                  </div>
                  <p style="color: #64748b; font-size: 12px; text-align: center;">If you did not request a password reset, please ignore this email.</p>
                </div>
                """
                    .formatted(otpCode);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Successfully sent OTP email to {}", recipientEmail);
          } catch (MessagingException e) {
            log.error("Failed to send OTP email to {}: {}", recipientEmail, e.getMessage());
          } catch (Exception e) {
            log.warn(
                "SMTP delivery attempt to {} completed with note: {}",
                recipientEmail,
                e.getMessage());
          }
        });
  }
}
