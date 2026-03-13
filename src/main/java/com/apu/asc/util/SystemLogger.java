package com.apu.asc.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
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

    try (BufferedWriter writer =
        Files.newBufferedWriter(
            Path.of(LOG_FILE),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND)) {
      writer.write(line);
      writer.newLine();
    } catch (IOException e) {
      System.err.println("Audit log write failed: " + e.getMessage());
    }
  }
}
