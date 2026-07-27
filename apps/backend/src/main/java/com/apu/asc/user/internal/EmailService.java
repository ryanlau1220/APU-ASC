package com.apu.asc.user.internal;

import jakarta.servlet.http.HttpServletRequest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

  private final JavaMailSender mailSender;
  private final TemplateEngine templateEngine;

  @Value("${spring.mail.username:}")
  private String fromEmail;

  public void sendOtpEmail(String recipientEmail, String otpCode, HttpServletRequest request) {
    String platformInfo = parsePlatform(request != null ? request.getHeader("User-Agent") : null);
    String clientIp = extractClientIp(request);
    String locationInfo = "Kuala Lumpur, Malaysia (" + clientIp + ")";

    sendOtpEmail(recipientEmail, otpCode, platformInfo, locationInfo);
  }

  public void sendOtpEmail(
      String recipientEmail, String otpCode, String platformInfo, String locationInfo) {
    CompletableFuture.runAsync(
        () -> {
          try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, "UTF-8");

            String sender =
                StringUtils.hasText(fromEmail) ? fromEmail.trim() : "noreply@apu-asc.com";

            helper.setFrom(sender);
            helper.setTo(recipientEmail);
            helper.setSubject("APU-ASC Password Reset Verification Code");

            String formattedTime =
                ZonedDateTime.now(ZoneId.of("Asia/Kuala_Lumpur"))
                    .format(
                        DateTimeFormatter.ofPattern(
                            "MMMM d, yyyy 'at' hh:mm:ss a 'GMT+8'", Locale.ENGLISH));

            String formattedCode =
                otpCode.length() == 6
                    ? otpCode.substring(0, 3) + "-" + otpCode.substring(3)
                    : otpCode;

            Context context = new Context();
            context.setVariable("subject", "APU-ASC Password Reset Verification Code");
            context.setVariable(
                "mainContent",
                "Enter this verification code in your APU-ASC Account to reset your password.");
            context.setVariable(
                "subContent",
                "Don't share this code with anyone. Our employees will never ask for the code.");
            context.setVariable("codeDisplay", formattedCode);
            context.setVariable("platformInfo", platformInfo);
            context.setVariable("locationInfo", locationInfo);
            context.setVariable("timeFormatted", formattedTime);

            String htmlContent = templateEngine.process("email/transactional-email", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Successfully sent Thymeleaf OTP email to {}", recipientEmail);
          } catch (Exception e) {
            log.error("SMTP delivery attempt to {} failed: {}", recipientEmail, e.getMessage(), e);
          }
        });
  }

  private String parsePlatform(String userAgent) {
    if (!StringUtils.hasText(userAgent)) {
      return "Web Portal on Desktop Device";
    }

    String os = "Desktop";
    if (userAgent.contains("Android")) os = "Android";
    else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) os = "iOS";
    else if (userAgent.contains("Linux")) os = "Linux";
    else if (userAgent.contains("Mac")) os = "Mac";
    else if (userAgent.contains("Windows")) os = "Windows";

    String browser = "Browser";
    if (userAgent.contains("Chrome")) browser = "Chrome browser";
    else if (userAgent.contains("Safari")) browser = "Safari browser";
    else if (userAgent.contains("Firefox")) browser = "Firefox browser";
    else if (userAgent.contains("Edg")) browser = "Edge browser";

    return browser + " on " + os + " device";
  }

  private String extractClientIp(HttpServletRequest request) {
    if (request == null) {
      return "localhost";
    }
    String forwarded = request.getHeader("X-Forwarded-For");
    if (StringUtils.hasText(forwarded)) {
      return forwarded.split(",")[0].trim();
    }
    String remote = request.getRemoteAddr();
    return StringUtils.hasText(remote) ? remote : "localhost";
  }
}
