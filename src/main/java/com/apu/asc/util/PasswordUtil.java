package com.apu.asc.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

  private PasswordUtil() {}

  public static String hash(String rawPassword) {
    return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
  }

  public static boolean verify(String rawPassword, String hash) {
    return BCrypt.checkpw(rawPassword, hash);
  }
}
