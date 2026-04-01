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
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AuditLogDAO implements IAuditLogDAO {

  private static final String FILE_PATH = "data/audit_logs.txt";
  private static final Path DATA_DIR = Path.of("data");
  private static AuditLogDAO instance;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
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

  public void reload() {
    load();
  }

  private void load() {
    lock.writeLock().lock();
    try {
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
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void append(AuditLog log) {
    lock.writeLock().lock();
    try {
      cache.add(log);
      Path path = Path.of(FILE_PATH);
      try {
        Files.createDirectories(DATA_DIR);
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
    } finally {
      lock.writeLock().unlock();
    }
  }

  public List<AuditLog> getAll() {
    lock.readLock().lock();
    try {
      return new ArrayList<>(cache);
    } finally {
      lock.readLock().unlock();
    }
  }
}
