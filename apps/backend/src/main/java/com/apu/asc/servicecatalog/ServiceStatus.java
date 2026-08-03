package com.apu.asc.servicecatalog;

/** Availability lifecycle for a catalog offering. */
public enum ServiceStatus {
  ACTIVE,
  INACTIVE;

  public static ServiceStatus fromString(String status) {
    if (status == null || status.isBlank()) {
      throw new IllegalArgumentException("A service status is required.");
    }
    try {
      return ServiceStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unsupported service status: " + status);
    }
  }

  public void requireTransitionTo(ServiceStatus target) {
    if (this != target && !canTransitionTo(target)) {
      throw new IllegalArgumentException(
          "Service cannot transition from " + this + " to " + target + ".");
    }
  }

  private boolean canTransitionTo(ServiceStatus target) {
    return (this == ACTIVE && target == INACTIVE) || (this == INACTIVE && target == ACTIVE);
  }
}
