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
import java.util.stream.Collectors;

public class AppointmentDAO {

  private static final String FILE_PATH = "data/appointments.txt";
  private static AppointmentDAO instance;
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

  private void load() {
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
  }

  private void persist() {
    try (BufferedWriter writer =
        Files.newBufferedWriter(Path.of(FILE_PATH), StandardCharsets.UTF_8)) {
      for (Appointment a : cache.values()) {
        writer.write(a.toFileString());
        writer.newLine();
      }
    } catch (IOException e) {
      System.err.println("AppointmentDAO persist failed: " + e.getMessage());
    }
  }

  public void save(Appointment appointment) {
    cache.put(appointment.getAppointmentId(), appointment);
    persist();
  }

  public Appointment findById(String id) {
    return cache.get(id);
  }

  public List<Appointment> findByCustomer(String customerId) {
    return cache.values().stream()
        .filter(a -> a.getCustomerId().equals(customerId))
        .collect(Collectors.toList());
  }

  public List<Appointment> findByTechnician(String technicianId) {
    return cache.values().stream()
        .filter(a -> a.getTechnicianId().equals(technicianId))
        .collect(Collectors.toList());
  }

  public List<Appointment> findByStatus(ApptStatus status) {
    return cache.values().stream()
        .filter(a -> a.getStatus() == status)
        .collect(Collectors.toList());
  }

  public List<Appointment> getAll() {
    return new ArrayList<>(cache.values());
  }
}
