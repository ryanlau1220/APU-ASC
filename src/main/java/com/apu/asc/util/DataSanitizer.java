package com.apu.asc.util;

public class DataSanitizer {

  private DataSanitizer() {}

  public static String clean(String input) {
    if (input == null) return "";
    return input.replace("||", "");
  }
}
