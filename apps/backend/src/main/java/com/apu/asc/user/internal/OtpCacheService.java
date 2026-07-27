package com.apu.asc.user.internal;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OtpCacheService {

  private final Map<String, OtpEntry> cache = new ConcurrentHashMap<>();

  public void putOtp(String email, String otpCode) {
    String key = email.trim().toLowerCase();
    Instant expiresAt = Instant.now().plus(10, ChronoUnit.MINUTES);
    cache.put(key, new OtpEntry(otpCode, expiresAt));
    log.info("Stored OTP in-memory cache for email {}, expiring at {}", key, expiresAt);
    cleanupExpired();
  }

  public boolean validateAndRemoveOtp(String email, String otpCode) {
    String key = email.trim().toLowerCase();
    OtpEntry entry = cache.get(key);
    if (entry == null) {
      log.warn("No OTP found in cache for email {}", key);
      return false;
    }

    if (Instant.now().isAfter(entry.expiresAt())) {
      log.warn("OTP for email {} has expired", key);
      cache.remove(key);
      return false;
    }

    if (!entry.otpCode().equals(otpCode.trim())) {
      log.warn("Mismatched OTP code for email {}", key);
      return false;
    }

    cache.remove(key);
    log.info("Successfully validated and invalidated OTP for email {}", key);
    return true;
  }

  private void cleanupExpired() {
    Instant now = Instant.now();
    cache.entrySet().removeIf(e -> now.isAfter(e.getValue().expiresAt()));
  }

  public record OtpEntry(String otpCode, Instant expiresAt) {}
}
