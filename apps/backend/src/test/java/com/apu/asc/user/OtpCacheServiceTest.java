package com.apu.asc.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.apu.asc.user.internal.OtpCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OtpCacheServiceTest {

  private OtpCacheService otpCacheService;

  @BeforeEach
  void setUp() {
    otpCacheService = new OtpCacheService();
  }

  @Test
  @DisplayName("Should validate valid OTP and remove it after single use")
  void testValidOtpSingleUse() {
    String email = "test@example.com";
    String otp = "123456";

    otpCacheService.putOtp(email, otp);

    // First attempt should succeed
    assertTrue(otpCacheService.validateAndRemoveOtp(email, otp));

    // Second attempt should fail (single use)
    assertFalse(otpCacheService.validateAndRemoveOtp(email, otp));
  }

  @Test
  @DisplayName("Should reject incorrect OTP code")
  void testMismatchedOtpCode() {
    String email = "test@example.com";
    otpCacheService.putOtp(email, "123456");

    assertFalse(otpCacheService.validateAndRemoveOtp(email, "654321"));
  }

  @Test
  @DisplayName("Should reject non-existent email OTP request")
  void testNonExistentEmailOtp() {
    assertFalse(otpCacheService.validateAndRemoveOtp("nonexistent@example.com", "123456"));
  }
}
