package com.apu.asc.payment;

import java.util.List;

public interface PaymentApi {
  List<PaymentDto> findAllPayments();

  PaymentDto getByAppointment(String appointmentId);

  PaymentDto createInvoice(PaymentDto paymentDto);

  PaymentDto processPayment(String paymentId, String method);
}
