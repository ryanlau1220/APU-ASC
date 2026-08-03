package com.apu.asc.payment;

public enum PaymentStatus {
  UNPAID,
  PAID,
  REFUNDED,
  FAILED;

  public static PaymentStatus fromString(String status) {
    if (status == null || status.isBlank()) {
      throw new IllegalArgumentException("A payment status is required.");
    }
    try {
      return PaymentStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Unsupported payment status: " + status);
    }
  }

  public void requireTransitionTo(PaymentStatus target) {
    if (this != target && !canTransitionTo(target)) {
      throw new IllegalArgumentException(
          "Payment cannot transition from " + this + " to " + target + ".");
    }
  }

  private boolean canTransitionTo(PaymentStatus target) {
    return switch (this) {
      case UNPAID -> target == PAID || target == FAILED;
      case PAID -> target == REFUNDED;
      case FAILED -> target == PAID;
      case REFUNDED -> false;
    };
  }
}
