package com.apu.asc.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class Payment {

  @NotBlank(message = "Payment ID must not be blank")
  private final String paymentId;

  @NotBlank(message = "Appointment ID must not be blank")
  private final String appointmentId;

  @DecimalMin(value = "0.01", message = "Amount paid must be at least RM 0.01")
  private final double amountPaid;

  private final double discountAmount;

  @NotNull(message = "Payment date and time must not be null")
  private final LocalDateTime paymentDateTime;

  private boolean receiptSent;

  public Payment(
      String paymentId,
      String appointmentId,
      double amountPaid,
      double discountAmount,
      LocalDateTime paymentDateTime,
      boolean receiptSent) {
    this.paymentId = paymentId;
    this.appointmentId = appointmentId;
    this.amountPaid = amountPaid;
    this.discountAmount = discountAmount;
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

  public double getDiscountAmount() {
    return discountAmount;
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
        String.valueOf(discountAmount),
        paymentDateTime.toString(),
        String.valueOf(receiptSent));
  }
}
