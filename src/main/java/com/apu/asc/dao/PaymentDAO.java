package com.apu.asc.dao;

import com.apu.asc.model.Payment;
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

public class PaymentDAO implements IPaymentDAO {

  private static final String FILE_PATH = "data/payments.txt";
  private static final Path DATA_DIR = Path.of("data");
  private static PaymentDAO instance;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Map<String, Payment> cache = new LinkedHashMap<>();

  private PaymentDAO() {
    load();
  }

  public static PaymentDAO getInstance() {
    if (instance == null) {
      instance = new PaymentDAO();
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
        if (p.length < 5) continue;
        double discount = (p.length >= 6) ? Double.parseDouble(p[3]) : 0.0;
        int dtIdx = (p.length >= 6) ? 4 : 3;
        int rcIdx = (p.length >= 6) ? 5 : 4;
        Payment payment =
            new Payment(
                p[0],
                p[1],
                Double.parseDouble(p[2]),
                discount,
                LocalDateTime.parse(p[dtIdx]),
                Boolean.parseBoolean(p[rcIdx]));
        cache.put(payment.getPaymentId(), payment);
      }
    } catch (IOException e) {
      System.err.println("PaymentDAO load failed: " + e.getMessage());
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
      System.err.println("PaymentDAO createDirectories failed: " + e.getMessage());
    }
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      for (Payment payment : cache.values()) {
        writer.write(payment.toFileString());
        writer.newLine();
      }
    } catch (IOException e) {
      System.err.println("PaymentDAO persist failed: " + e.getMessage());
    }
  }

  public void save(Payment payment) {
    lock.writeLock().lock();
    try {
      cache.put(payment.getPaymentId(), payment);
    persist();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public Payment findByAppointment(String appointmentId) {
    lock.readLock().lock();
    try {
      return cache.values().stream()
        .filter(p -> p.getAppointmentId().equals(appointmentId))
        .findFirst()
        .orElse(null);
    } finally {
      lock.readLock().unlock();
    }
  }

  public List<Payment> getAll() {
    lock.readLock().lock();
    try {
      return new ArrayList<>(cache.values());
    } finally {
      lock.readLock().unlock();
    }
  }
}
