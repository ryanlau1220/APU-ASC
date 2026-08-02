package com.apu.asc.payment;

public enum PaymentStatus {
  UNPAID,
  PAID,
  REFUNDED,
  FAILED;

  public static PaymentStatus fromString(String status) {
    if (status == null || status.isBlank()) {
      return UNPAID;
    }
    try {
      return PaymentStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      return UNPAID;
    }
  }
}
