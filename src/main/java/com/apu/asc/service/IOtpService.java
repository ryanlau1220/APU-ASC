package com.apu.asc.service;

import java.util.concurrent.CompletableFuture;

public interface IOtpService {
  public String generateOtp(String username);
  public boolean validateOtp(String username, String code);
  public void clearOtp(String username);
  public CompletableFuture<Void> sendOtpEmail(String toEmail, String username, String otp);
}
