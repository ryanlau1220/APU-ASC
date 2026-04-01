package com.apu.asc.service;

import com.apu.asc.util.I18n;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

public interface IOtpService {
  public String generateOtp(String username);
  public boolean validateOtp(String username, String code);
  public void clearOtp(String username);
  public CompletableFuture<Void> sendOtpEmail(String toEmail, String username, String otp);
}
