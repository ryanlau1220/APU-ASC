package com.apu.asc.dao;

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

import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import com.apu.asc.model.UserStatus;
import com.apu.asc.util.UserFactory;

public class UserDAO implements IUserDAO {

  private static final String FILE_PATH = "data/users.txt";
  private static final Path DATA_DIR = Path.of("data");
  private static UserDAO instance;
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final Map<String, User> cache = new LinkedHashMap<>();

  private UserDAO() {
    load();
  }

  public static UserDAO getInstance() {
    if (instance == null) {
      instance = new UserDAO();
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

    if (!Files.exists(DATA_DIR)) {
      try {
        Files.createDirectories(DATA_DIR);
      } catch (IOException e) {
        System.err.println("Could not create data dir");
      }
    }
    if (!Files.exists(path)) {
      try {
        Files.createFile(path);
        // Create default admin
        User admin = UserFactory.fromFileLine("USR-ADMIN||admin||3CqYnAErVFulfApGhzq6Tg==:tz/v/C2a7NLOC4g3PO4qF3fiGqsKLtB1pcmnjz5r8c4=||MANAGER||System Administrator||admin@apu-asc.com||1234567890||ACTIVE");
        if (admin != null) {
            save(admin);
        }
      } catch (IOException e) {
        System.err.println("Could not create users.txt");
      }
      return;
    }


    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) continue;
        User user = UserFactory.fromFileLine(line);
        if (user != null) cache.put(user.getId(), user);
      }
    } catch (IOException e) {
      System.err.println("UserDAO load failed: " + e.getMessage());
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
      System.err.println("UserDAO createDirectories failed: " + e.getMessage());
    }
    try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
      for (User user : cache.values()) {
        writer.write(user.toFileString());
        writer.newLine();
      }
    } catch (IOException e) {
      System.err.println("UserDAO persist failed: " + e.getMessage());
    }
  }

  @Override
  public void save(User user) {
    lock.writeLock().lock();
    try {
      cache.put(user.getId(), user);
    persist();
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public User findById(String id) {
    lock.readLock().lock();
    try {
      return cache.get(id);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public User findByUsername(String username) {
    lock.readLock().lock();
    try {
      return cache.values().stream()
        .filter(u -> u.getUsername().equalsIgnoreCase(username))
        .findFirst()
        .orElse(null);
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public List<User> getAll() {
    lock.readLock().lock();
    try {
      return new ArrayList<>(cache.values());
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public List<User> getAllByRole(Role role) {
    lock.readLock().lock();
    try {
      return cache.values().stream().filter(u -> u.getRole() == role).collect(Collectors.toList());
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public void deactivate(String id) {
    lock.writeLock().lock();
    try {
      User user = cache.get(id);
    if (user != null) {
      user.setStatus(UserStatus.DEACTIVATED);
      persist();
    }
    } finally {
      lock.writeLock().unlock();
    }
  }
}
