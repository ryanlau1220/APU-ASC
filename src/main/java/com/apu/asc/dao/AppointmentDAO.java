package com.apu.asc.dao;

import com.apu.asc.model.Appointment;
import com.apu.asc.model.ApptStatus;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class AppointmentDAO implements IAppointmentDAO {

  private static final String FILE_PATH = "data/appointments.txt";
  private static final Path DATA_DIR = Path.of("data");
  private static AppointmentDAO instance;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Map<String, Appointment> cache = new LinkedHashMap<>();

  private AppointmentDAO() {
    load();
  }

  public static AppointmentDAO getInstance() {
    if (instance == null) {
      instance = new AppointmentDAO();
    }
    return instance;
  }

  @Override
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
        if (p.length < 9) continue;
        Appointment a =
            new Appointment(
                p[0],
                p[1],
                p[2],
                p[3],
                p[4],
                p[5],
                LocalDateTime.parse(p[6]),
                ApptStatus.valueOf(p[7]),
                p[8]);
        cache.put(a.getAppointmentId(), a);
      }
    } catch (IOException e) {
      System.err.println("AppointmentDAO load failed: " + e.getMessage());
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
      System.err.println("AppointmentDAO createDirectories failed: " + e.getMessage());
    }
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      for (Appointment a : cache.values()) {
        writer.write(a.toFileString());
        writer.newLine();
      }
    } catch (IOException e) {
      System.err.println("AppointmentDAO persist failed: " + e.getMessage());
    }
  }

  @Override
  public void save(Appointment appointment) {
    lock.writeLock().lock();
    try {
      cache.put(appointment.getAppointmentId(), appointment);
    persist();
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public Appointment findById(String id) {
    lock.readLock().lock();
    try {
      return cache.get(id);
    } finally {
      lock.readLock().unlock();
    }
  }

  public List<Appointment> findByCustomer(String customerId) {
    lock.readLock().lock();
    try {
      return cache.values().stream()
        .filter(a -> a.getCustomerId().equals(customerId))
        .collect(Collectors.toList());
    } finally {
      lock.readLock().unlock();
    }
  }

  public List<Appointment> findByTechnician(String technicianId) {
    lock.readLock().lock();
    try {
      return cache.values().stream()
        .filter(a -> a.getTechnicianId().equals(technicianId))
        .collect(Collectors.toList());
    } finally {
      lock.readLock().unlock();
    }
  }

  public List<Appointment> findByStatus(ApptStatus status) {
    lock.readLock().lock();
    try {
      return cache.values().stream()
        .filter(a -> a.getStatus() == status)
        .collect(Collectors.toList());
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public List<Appointment> getAll() {
    lock.readLock().lock();
    try {
      return new ArrayList<>(cache.values());
    } finally {
      lock.readLock().unlock();
    }
  }
}
