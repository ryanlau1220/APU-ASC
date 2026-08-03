package com.apu.asc.workorder;

/** The workshop execution lifecycle. Bookings remain in the appointment lifecycle. */
public enum WorkOrderStatus {
  OPEN,
  DIAGNOSING,
  IN_PROGRESS,
  COMPLETED,
  CANCELLED;

  public static WorkOrderStatus fromString(String status) {
    if (status == null || status.isBlank()) {
      throw new IllegalArgumentException("A work order status is required.");
    }
    try {
      return WorkOrderStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unsupported work order status: " + status);
    }
  }

  public void requireTransitionTo(WorkOrderStatus target) {
    if (this != target && !canTransitionTo(target)) {
      throw new IllegalArgumentException(
          "Work order cannot transition from " + this + " to " + target + ".");
    }
  }

  private boolean canTransitionTo(WorkOrderStatus target) {
    return switch (this) {
      case OPEN -> target == DIAGNOSING || target == CANCELLED;
      case DIAGNOSING -> target == IN_PROGRESS || target == CANCELLED;
      case IN_PROGRESS -> target == COMPLETED || target == CANCELLED;
      case COMPLETED, CANCELLED -> false;
    };
  }
}
