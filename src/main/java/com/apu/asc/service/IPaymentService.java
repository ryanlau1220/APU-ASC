package com.apu.asc.service;

import com.apu.asc.model.Payment;
import com.apu.asc.util.Result;
import java.util.concurrent.CompletableFuture;

public interface IPaymentService {
  public Result<Payment> processPayment(String appointmentId);
  public CompletableFuture<Boolean> sendReceipt(String paymentId);
  public Payment findByAppointment(String appointmentId);
}
