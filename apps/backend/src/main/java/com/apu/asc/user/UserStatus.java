package com.apu.asc.user;

public enum UserStatus {
  ACTIVE,
  PENDING_VERIFICATION,
  INACTIVE;

  public static UserStatus fromString(String status) {
    if (status == null || status.isBlank()) {
      return ACTIVE;
    }
    try {
      return UserStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      return ACTIVE;
    }
  }
}
