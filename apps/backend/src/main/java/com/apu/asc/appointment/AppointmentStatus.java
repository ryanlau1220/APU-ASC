package com.apu.asc.appointment;

public enum AppointmentStatus {
  PENDING,
  CONFIRMED,
  IN_PROGRESS,
  COMPLETED,
  CANCELLED;

  public static AppointmentStatus fromString(String status) {
    if (status == null || status.isBlank()) {
      return PENDING;
    }
    try {
      return AppointmentStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      return PENDING;
    }
  }
}
