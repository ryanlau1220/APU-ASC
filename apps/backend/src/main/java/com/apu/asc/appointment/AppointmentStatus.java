package com.apu.asc.appointment;

public enum AppointmentStatus {
  PENDING,
  CONFIRMED,
  CANCELLED;

  public static AppointmentStatus fromString(String status) {
    if (status == null || status.isBlank()) {
      throw new IllegalArgumentException("An appointment status is required.");
    }
    try {
      return AppointmentStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unsupported appointment status: " + status);
    }
  }

  public void requireTransitionTo(AppointmentStatus target) {
    if (this != target && !canTransitionTo(target)) {
      throw new IllegalArgumentException(
          "Appointment cannot transition from " + this + " to " + target + ".");
    }
  }

  private boolean canTransitionTo(AppointmentStatus target) {
    return switch (this) {
      case PENDING -> target == CONFIRMED || target == CANCELLED;
      case CONFIRMED -> target == CANCELLED;
      case CANCELLED -> false;
    };
  }
}
