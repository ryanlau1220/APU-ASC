package com.apu.asc.user;

public enum UserStatus {
  PENDING_VERIFICATION,
  ACTIVE,
  INACTIVE;

  public static UserStatus fromString(String status) {
    if (status == null || status.isBlank()) {
      throw new IllegalArgumentException("A user status is required.");
    }
    try {
      return UserStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unsupported user status: " + status);
    }
  }

  public void requireTransitionTo(UserStatus target) {
    if (this != target && !canTransitionTo(target)) {
      throw new IllegalArgumentException(
          "User account cannot transition from " + this + " to " + target + ".");
    }
  }

  private boolean canTransitionTo(UserStatus target) {
    return switch (this) {
      case PENDING_VERIFICATION -> target == ACTIVE || target == INACTIVE;
      case ACTIVE -> target == INACTIVE;
      case INACTIVE -> target == ACTIVE;
    };
  }
}
