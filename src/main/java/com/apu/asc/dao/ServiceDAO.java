package com.apu.asc.dao;

import com.apu.asc.model.Service;
import com.apu.asc.model.ServiceType;
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

public class ServiceDAO implements IServiceDAO {

  private static final String FILE_PATH = "data/services.txt";
  private static final Path DATA_DIR = Path.of("data");
  private static ServiceDAO instance;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Map<String, Service> cache = new LinkedHashMap<>();

  private ServiceDAO() {
    load();
  }

  public static ServiceDAO getInstance() {
    if (instance == null) {
      instance = new ServiceDAO();
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
        if (p.length < 5) continue;
        Service s =
            new Service(
                p[0],
                ServiceType.valueOf(p[1]),
                p[2],
                Double.parseDouble(p[3]),
                Boolean.parseBoolean(p[4]));
        cache.put(s.getServiceId(), s);
      }
    } catch (IOException e) {
      System.err.println("ServiceDAO load failed: " + e.getMessage());
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
      System.err.println("ServiceDAO createDirectories failed: " + e.getMessage());
    }
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      for (Service s : cache.values()) {
        writer.write(s.toFileString());
        writer.newLine();
      }
    } catch (IOException e) {
      System.err.println("ServiceDAO persist failed: " + e.getMessage());
    }
  }

  @Override
  public void save(Service service) {
    lock.writeLock().lock();
    try {
      cache.put(service.getServiceId(), service);
    persist();
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public Service findById(String id) {
    lock.readLock().lock();
    try {
      return cache.get(id);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public List<Service> getActiveServices() {
    lock.readLock().lock();
    try {
      return cache.values().stream().filter(Service::isActive).collect(Collectors.toList());
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public List<Service> getAll() {
    lock.readLock().lock();
    try {
      return new ArrayList<>(cache.values());
    } finally {
      lock.readLock().unlock();
    }
  }
}
