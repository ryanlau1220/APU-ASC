package com.apu.asc.user.internal;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpCacheService {

  @Autowired(required = false)
  private StringRedisTemplate redisTemplate;

  private final Map<String, OtpEntry> fallbackCache = new ConcurrentHashMap<>();

  public void putOtp(String email, String otpCode) {
    String emailKey = email.trim().toLowerCase();
    String redisKey = "otp:" + emailKey;
    try {
      if (redisTemplate != null) {
        redisTemplate.opsForValue().set(redisKey, otpCode, Duration.ofMinutes(10));
        log.info("Stored OTP in Redis/Valkey cache for email {}", emailKey);
        return;
      }
    } catch (Exception e) {
      log.warn("Redis unavailable, using fallback in-memory OTP cache: {}", e.getMessage());
    }
    fallbackCache.put(emailKey, new OtpEntry(otpCode, Instant.now().plus(10, ChronoUnit.MINUTES)));
  }

  public boolean validateAndRemoveOtp(String email, String otpCode) {
    String emailKey = email.trim().toLowerCase();
    String redisKey = "otp:" + emailKey;
    String trimmedOtp = otpCode.trim();

    try {
      if (redisTemplate != null) {
        String cachedOtp = redisTemplate.opsForValue().get(redisKey);
        if (cachedOtp != null) {
          if (cachedOtp.equals(trimmedOtp)) {
            redisTemplate.delete(redisKey);
            log.info("Validated and removed OTP from Redis for email {}", emailKey);
            return true;
          }
          log.warn("Mismatched OTP code for email {}", emailKey);
          return false;
        }
      }
    } catch (Exception e) {
      log.warn("Redis lookup failed, falling back to in-memory cache: {}", e.getMessage());
    }

    OtpEntry entry = fallbackCache.get(emailKey);
    if (entry == null) {
      log.warn("No OTP found in cache for email {}", emailKey);
      return false;
    }

    if (Instant.now().isAfter(entry.expiresAt())) {
      fallbackCache.remove(emailKey);
      log.warn("OTP for email {} has expired", emailKey);
      return false;
    }

    if (!entry.otpCode().equals(trimmedOtp)) {
      log.warn("Mismatched OTP code for email {}", emailKey);
      return false;
    }

    fallbackCache.remove(emailKey);
    log.info("Validated and removed OTP from in-memory cache for email {}", emailKey);
    return true;
  }

  public record OtpEntry(String otpCode, Instant expiresAt) {}
}
