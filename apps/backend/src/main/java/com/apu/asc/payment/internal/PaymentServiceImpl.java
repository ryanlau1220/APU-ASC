package com.apu.asc.payment.internal;

import com.apu.asc.payment.PaymentApi;
import com.apu.asc.payment.PaymentDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class PaymentServiceImpl implements PaymentApi {

  private final PaymentRepository paymentRepository;

  @Override
  @Transactional(readOnly = true)
  public List<PaymentDto> findAllPayments() {
    return paymentRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentDto getByAppointment(final String appointmentId) {
    return paymentRepository
        .findByAppointmentId(appointmentId)
        .map(this::toDto)
        .orElseThrow(
            () -> new RuntimeException("Payment not found for appointment: " + appointmentId));
  }

  @Override
  @Transactional
  public PaymentDto createInvoice(final PaymentDto paymentDto) {
    String id =
        paymentDto.id() != null
            ? paymentDto.id()
            : "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    String invoiceNum =
        paymentDto.invoiceNumber() != null
            ? paymentDto.invoiceNumber()
            : "INV-" + System.currentTimeMillis();
    PaymentEntity entity =
        PaymentEntity.builder()
            .id(id)
            .appointmentId(paymentDto.appointmentId())
            .customerId(paymentDto.customerId())
            .invoiceNumber(invoiceNum)
            .amount(paymentDto.amount())
            .paymentMethod(paymentDto.paymentMethod())
            .paymentStatus(
                paymentDto.paymentStatus() != null ? paymentDto.paymentStatus() : "UNPAID")
            .build();
    return toDto(paymentRepository.save(entity));
  }

  @Override
  @Transactional
  public PaymentDto processPayment(final String paymentId, final String method) {
    PaymentEntity payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    payment.setPaymentMethod(method);
    payment.setPaymentStatus("PAID");
    payment.setPaidAt(Instant.now());
    return toDto(paymentRepository.save(payment));
  }

  private PaymentDto toDto(PaymentEntity entity) {
    return new PaymentDto(
        entity.getId(),
        entity.getAppointmentId(),
        entity.getCustomerId(),
        entity.getInvoiceNumber(),
        entity.getAmount(),
        entity.getPaymentMethod(),
        entity.getPaymentStatus(),
        entity.getPaidAt(),
        entity.getCreatedAt());
  }
}
