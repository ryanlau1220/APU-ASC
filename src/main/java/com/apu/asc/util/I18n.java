package com.apu.asc.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class I18n {

  private static Locale locale = Locale.ENGLISH;
  private static ResourceBundle bundle = ResourceBundle.getBundle("lang/messages", locale);

  private I18n() {}

  public static void setLocale(Locale newLocale) {
    locale = newLocale == null ? Locale.ENGLISH : newLocale;
    bundle = ResourceBundle.getBundle("lang/messages", locale);
  }

  public static Locale getLocale() {
    return locale;
  }

  public static String t(String key) {
    try {
      return bundle.getString(key);
    } catch (MissingResourceException e) {
      return key;
    }
  }

  public static String format(String key, Object... args) {
    return String.format(locale, t(key), args);
  }

  public static String resolveError(String error) {
    if (error == null || !error.startsWith("err.")) return error;
    String[] parts = error.split("\\|", -1);
    String key = parts[0];
    if (parts.length == 1) return t(key);
    Object[] args = Arrays.copyOfRange(parts, 1, parts.length);
    return String.format(locale, t(key), args);
  }
}
