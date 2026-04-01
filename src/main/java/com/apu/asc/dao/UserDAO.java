package com.apu.asc.dao;

import com.apu.asc.model.Role;
import com.apu.asc.model.User;
import com.apu.asc.model.UserStatus;
import com.apu.asc.util.UserFactory;
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

public class UserDAO {

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

  public void save(User user) {
    lock.writeLock().lock();
    try {
      cache.put(user.getId(), user);
    persist();
    } finally {
      lock.writeLock().unlock();
    }
  }

  public User findById(String id) {
    lock.readLock().lock();
    try {
      return cache.get(id);
    } finally {
      lock.readLock().unlock();
    }
  }

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

  public List<User> getAll() {
    lock.readLock().lock();
    try {
      return new ArrayList<>(cache.values());
    } finally {
      lock.readLock().unlock();
    }
  }

  public List<User> getAllByRole(Role role) {
    lock.readLock().lock();
    try {
      return cache.values().stream().filter(u -> u.getRole() == role).collect(Collectors.toList());
    } finally {
      lock.readLock().unlock();
    }
  }

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
