package com.apu.asc.user;

public enum UserRole {
  CUSTOMER,
  STAFF,
  TECHNICIAN,
  MANAGER,
  WORKSHOP_MANAGER,
  SYSTEM_ADMIN;

  public static UserRole fromString(String role) {
    if (role == null || role.isBlank()) {
      return CUSTOMER;
    }
    String cleanRole = role.toUpperCase().replace("ROLE_", "");
    try {
      return UserRole.valueOf(cleanRole);
    } catch (IllegalArgumentException e) {
      return CUSTOMER;
    }
  }
}
