package com.apu.asc.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordUtil {

  private static final int ITERATIONS = 65536;
  private static final int KEY_LENGTH = 256;
  private static final String ALGORITHM = "PBKDF2WithHmacSHA256";

  private PasswordUtil() {}

  public static String hash(String rawPassword) {
    try {
      byte[] salt = new byte[16];
      new SecureRandom().nextBytes(salt);
      
      PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
      SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
      byte[] hash = skf.generateSecret(spec).getEncoded();
      
      return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new RuntimeException("Error hashing password", e);
    }
  }

  public static boolean verify(String rawPassword, String storedHash) {
    try {
      String[] parts = storedHash.split(":");
      if (parts.length != 2) return false;
      
      byte[] salt = Base64.getDecoder().decode(parts[0]);
      byte[] hash = Base64.getDecoder().decode(parts[1]);
      
      PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
      SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
      byte[] testHash = skf.generateSecret(spec).getEncoded();
      
      if (hash.length != testHash.length) return false;
      for (int i = 0; i < hash.length; i++) {
        if (hash[i] != testHash[i]) return false;
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
