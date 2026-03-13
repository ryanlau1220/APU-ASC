package com.apu.asc.util;

import com.apu.asc.model.*;

public class UserFactory {

  private UserFactory() {}

  public static User fromFileLine(String line) {
    String[] parts = line.split("\\|\\|", -1);
    if (parts.length < 8) return null;

    String id = parts[0];
    String username = parts[1];
    String passwordHash = parts[2];
    Role role = Role.valueOf(parts[3]);
    String fullName = parts[4];
    String email = parts[5];
    String contactNumber = parts[6];
    UserStatus status = UserStatus.valueOf(parts[7]);

    return switch (role) {
      case CUSTOMER ->
          new Customer(id, username, passwordHash, status, fullName, email, contactNumber);
      case STAFF ->
          new CounterStaff(id, username, passwordHash, status, fullName, email, contactNumber);
      case TECHNICIAN ->
          new Technician(id, username, passwordHash, status, fullName, email, contactNumber);
      case MANAGER ->
          new Manager(id, username, passwordHash, status, fullName, email, contactNumber);
    };
  }
}
