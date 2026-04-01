package com.apu.asc.dao;

import com.apu.asc.model.Feedback;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class FeedbackDAO implements IFeedbackDAO {

  private static final String FILE_PATH = "data/feedbacks.txt";
  private static final Path DATA_DIR = Path.of("data");
  private static FeedbackDAO instance;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Map<String, Feedback> cache = new LinkedHashMap<>();

  private FeedbackDAO() {
    load();
  }

  public static FeedbackDAO getInstance() {
    if (instance == null) {
      instance = new FeedbackDAO();
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
        if (p.length < 4) continue;
        Feedback f = new Feedback(p[0], p[1], Integer.parseInt(p[2]), p[3]);
        cache.put(f.getFeedbackId(), f);
      }
    } catch (IOException e) {
      System.err.println("FeedbackDAO load failed: " + e.getMessage());
    }
    } finally {
      lock.writeLock().unlock();
    }
  }

  private void persist() {
    Path path = Path.of(FILE_PATH);
    try {
      Files.createDirectories(DATA_DIR);
    } catch (IOException e) {
      System.err.println("FeedbackDAO createDirectories failed: " + e.getMessage());
    }
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      for (Feedback f : cache.values()) {
        writer.write(f.toFileString());
        writer.newLine();
      }
    } catch (IOException e) {
      System.err.println("FeedbackDAO persist failed: " + e.getMessage());
    }
  }

  public void save(Feedback feedback) {
    lock.writeLock().lock();
    try {
      cache.put(feedback.getFeedbackId(), feedback);
    persist();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public Feedback findByAppointment(String appointmentId) {
    lock.readLock().lock();
    try {
      return cache.values().stream()
        .filter(f -> f.getAppointmentId().equals(appointmentId))
        .findFirst()
        .orElse(null);
    } finally {
      lock.readLock().unlock();
    }
  }

  public List<Feedback> findByTechnician(String technicianId, AppointmentDAO appointmentDAO) {
    lock.readLock().lock();
    try {
      return cache.values().stream()
        .filter(
            f -> {
              var appt = appointmentDAO.findById(f.getAppointmentId());
              return appt != null && appt.getTechnicianId().equals(technicianId);
            })
        .collect(Collectors.toList());
    } finally {
      lock.readLock().unlock();
    }
  }

  public List<Feedback> getAll() {
    lock.readLock().lock();
    try {
      return new ArrayList<>(cache.values());
    } finally {
      lock.readLock().unlock();
    }
  }
}
