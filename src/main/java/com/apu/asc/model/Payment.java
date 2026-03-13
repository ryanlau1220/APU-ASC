package com.apu.asc.model;

import java.time.LocalDateTime;

public class Payment {

  private final String paymentId;
  private final String appointmentId;
  private final double amountPaid;
  private final LocalDateTime paymentDateTime;
  private boolean receiptSent;

  public Payment(
      String paymentId,
      String appointmentId,
      double amountPaid,
      LocalDateTime paymentDateTime,
      boolean receiptSent) {
    this.paymentId = paymentId;
    this.appointmentId = appointmentId;
    this.amountPaid = amountPaid;
    this.paymentDateTime = paymentDateTime;
    this.receiptSent = receiptSent;
  }

  public String getPaymentId() {
    return paymentId;
  }

  public String getAppointmentId() {
    return appointmentId;
  }

  public double getAmountPaid() {
    return amountPaid;
  }

  public LocalDateTime getPaymentDateTime() {
    return paymentDateTime;
  }

  public boolean isReceiptSent() {
    return receiptSent;
  }

  public void markReceiptSent() {
    this.receiptSent = true;
  }

  public String toFileString() {
    return String.join(
        "||",
        paymentId,
        appointmentId,
        String.valueOf(amountPaid),
        paymentDateTime.toString(),
        String.valueOf(receiptSent));
  }
}
