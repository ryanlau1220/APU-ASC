package com.apu.asc.ui.util;

import java.util.Locale;
import java.util.ResourceBundle;

public final class LanguageManager {

  private static final LanguageManager INSTANCE = new LanguageManager();

  private Locale locale = Locale.ENGLISH;
  private ResourceBundle bundle = ResourceBundle.getBundle("lang/messages", locale);

  private LanguageManager() {}

  public static LanguageManager getInstance() {
    return INSTANCE;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
    this.bundle = ResourceBundle.getBundle("lang/messages", locale);
  }

  public Locale getLocale() {
    return locale;
  }

  public String get(String key) {
    try {
      return bundle.getString(key);
    } catch (java.util.MissingResourceException e) {
      return key;
    }
  }

  public static String t(String key) {
    return INSTANCE.get(key);
  }
}
