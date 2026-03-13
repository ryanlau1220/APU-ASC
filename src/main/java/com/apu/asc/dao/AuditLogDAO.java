package com.apu.asc.dao;

import com.apu.asc.model.AuditLog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

  private static final String FILE_PATH = "data/audit_logs.txt";
  private static AuditLogDAO instance;
  private final List<AuditLog> cache = new ArrayList<>();

  private AuditLogDAO() {
    load();
  }

  public static AuditLogDAO getInstance() {
    if (instance == null) {
      instance = new AuditLogDAO();
    }
    return instance;
  }

  private void load() {
    cache.clear();
    Path path = Path.of(FILE_PATH);
    if (!Files.exists(path)) return;

    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) continue;
        String[] p = line.split("\\|\\|", -1);
        if (p.length < 6) continue;
        cache.add(new AuditLog(p[0], LocalDateTime.parse(p[1]), p[2], p[3], p[4], p[5]));
      }
    } catch (IOException e) {
      System.err.println("AuditLogDAO load failed: " + e.getMessage());
    }
  }

  public void append(AuditLog log) {
    cache.add(log);
    Path path = Path.of(FILE_PATH);
    try {
      Files.createDirectories(path.getParent());
    } catch (IOException e) {
      System.err.println("AuditLogDAO createDirectories failed: " + e.getMessage());
    }
    try (BufferedWriter writer =
        Files.newBufferedWriter(
            path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
      writer.write(log.toFileString());
      writer.newLine();
    } catch (IOException e) {
      System.err.println("AuditLogDAO append failed: " + e.getMessage());
    }
  }

  public List<AuditLog> getAll() {
    return new ArrayList<>(cache);
  }
}
