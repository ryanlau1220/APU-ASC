package com.apu.asc.payment;

public enum PaymentMethod {
  PENDING,
  CREDIT_CARD,
  DEBIT_CARD,
  ONLINE_BANKING,
  CASH,
  E_WALLET,
  STRIPE_CHECKOUT;

  public static PaymentMethod fromString(String method) {
    if (method == null || method.isBlank()) {
      return PENDING;
    }
    try {
      return PaymentMethod.valueOf(method.toUpperCase().replace(" ", "_"));
    } catch (IllegalArgumentException e) {
      return PENDING;
    }
  }
}
