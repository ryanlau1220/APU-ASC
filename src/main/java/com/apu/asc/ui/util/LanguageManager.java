package com.apu.asc.ui.util;

import com.apu.asc.util.I18n;
import java.util.Arrays;
import java.util.Locale;

public final class LanguageManager {

  private static final LanguageManager INSTANCE = new LanguageManager();

  private LanguageManager() {}

  public static LanguageManager getInstance() {
    return INSTANCE;
  }

  public void setLocale(Locale locale) {
    I18n.setLocale(locale);
  }

  public Locale getLocale() {
    return I18n.getLocale();
  }

  public static String t(String key) {
    return I18n.t(key);
  }

  public static String format(String key, Object... args) {
    return I18n.format(key, args);
  }

  public static String resolveError(String error) {
    if (error == null || !error.startsWith("err.")) return error;
    String[] parts = error.split("\\|", -1);
    String key = parts[0];
    if (parts.length == 1) return t(key);
    Object[] args = Arrays.copyOfRange(parts, 1, parts.length);
    return String.format(I18n.getLocale(), t(key), args);
  }
}
