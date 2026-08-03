package com.apu.asc.payment;

import java.util.List;

public interface PaymentApi {
  List<PaymentDto> findAllPayments();

  PaymentDto getPaymentById(String id);

  PaymentDto getByAppointment(String appointmentId);

  PaymentDto getByWorkOrder(String workOrderId);

  List<PaymentDto> getByCustomer(String customerId);

  PaymentDto createInvoice(PaymentDto paymentDto);

  PaymentDto updatePayment(String id, PaymentDto paymentDto);

  PaymentDto processPayment(String paymentId, String method);

  void deletePayment(String id);
}
