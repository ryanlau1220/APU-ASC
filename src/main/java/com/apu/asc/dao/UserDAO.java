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
import java.util.stream.Collectors;

public class UserDAO {

  private static final String FILE_PATH = "data/users.txt";
  private static final Path DATA_DIR = Path.of("data");
  private static UserDAO instance;
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

  private void load() {
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
    cache.put(user.getId(), user);
    persist();
  }

  public User findById(String id) {
    return cache.get(id);
  }

  public User findByUsername(String username) {
    return cache.values().stream()
        .filter(u -> u.getUsername().equalsIgnoreCase(username))
        .findFirst()
        .orElse(null);
  }

  public List<User> getAll() {
    return new ArrayList<>(cache.values());
  }

  public List<User> getAllByRole(Role role) {
    return cache.values().stream().filter(u -> u.getRole() == role).collect(Collectors.toList());
  }

  public void deactivate(String id) {
    User user = cache.get(id);
    if (user != null) {
      user.setStatus(UserStatus.DEACTIVATED);
      persist();
    }
  }
}
