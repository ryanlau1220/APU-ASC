package com.apu.asc.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class SystemLogger {

  private static final String LOG_FILE = "data/audit_logs.txt";

  private SystemLogger() {}

  public static void log(
      String userId, String actionType, String targetEntityId, String description) {
    String logId = "LOG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    String line =
        String.join(
            "||",
            logId,
            LocalDateTime.now().toString(),
            userId,
            actionType,
            targetEntityId,
            description);

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
      writer.write(line);
      writer.newLine();
    } catch (IOException e) {
      System.err.println("Audit log write failed: " + e.getMessage());
    }
  }
}
