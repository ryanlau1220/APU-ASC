package com.apu.asc.user.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
class InvitationTokenService {

  private static final int TOKEN_BYTES = 32;

  private final SecureRandom secureRandom = new SecureRandom();

  InvitationToken issue() {
    byte[] tokenBytes = new byte[TOKEN_BYTES];
    secureRandom.nextBytes(tokenBytes);
    String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    return new InvitationToken(rawToken, hash(rawToken));
  }

  String hash(String rawToken) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }

  record InvitationToken(String rawToken, String tokenHash) {}
}
