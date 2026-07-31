package com.apu.asc.payment.internal;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.payment.PaymentApi;
import com.apu.asc.payment.PaymentDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class PaymentServiceImpl implements PaymentApi {

  private final PaymentRepository paymentRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional(readOnly = true)
  public List<PaymentDto> findAllPayments() {
    return paymentRepository.findAll().stream().map(this::toDto).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentDto getPaymentById(final String id) {
    return paymentRepository
        .findById(id)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentDto getByAppointment(final String appointmentId) {
    return paymentRepository
        .findByAppointmentId(appointmentId)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Payment for appointment", appointmentId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentDto> getByCustomer(final String customerId) {
    return paymentRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
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
    PaymentDto created = toDto(paymentRepository.save(entity));
    eventPublisher.publishEvent(
        new AuditEvent(
            created.customerId(),
            "INVOICE_CREATED",
            "PAYMENT",
            created.id(),
            "Generated invoice " + created.invoiceNumber() + " for amount " + created.amount()));
    return created;
  }

  @Override
  @Transactional
  public PaymentDto updatePayment(final String id, final PaymentDto paymentDto) {
    PaymentEntity entity =
        paymentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", id));

    if (paymentDto.amount() != null) entity.setAmount(paymentDto.amount());
    if (paymentDto.paymentMethod() != null) entity.setPaymentMethod(paymentDto.paymentMethod());
    if (paymentDto.paymentStatus() != null) entity.setPaymentStatus(paymentDto.paymentStatus());

    PaymentDto updated = toDto(paymentRepository.save(entity));
    eventPublisher.publishEvent(
        new AuditEvent(
            updated.customerId(),
            "INVOICE_UPDATED",
            "PAYMENT",
            updated.id(),
            "Updated payment/invoice details"));
    return updated;
  }

  @Override
  @Transactional
  public PaymentDto processPayment(final String paymentId, final String method) {
    PaymentEntity payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
    payment.setPaymentMethod(method);
    payment.setPaymentStatus("PAID");
    payment.setPaidAt(Instant.now());
    PaymentDto updated = toDto(paymentRepository.save(payment));
    eventPublisher.publishEvent(
        new AuditEvent(
            updated.customerId(),
            "PAYMENT_PROCESSED",
            "PAYMENT",
            updated.id(),
            "Processed payment of " + updated.amount() + " via " + method));
    return updated;
  }

  @Override
  @Transactional
  public void deletePayment(final String id) {
    PaymentEntity payment =
        paymentRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", id));
    paymentRepository.deleteById(id);
    eventPublisher.publishEvent(
        new AuditEvent(
            payment.getCustomerId(),
            "PAYMENT_DELETED",
            "PAYMENT",
            id,
            "Deleted payment/invoice record"));
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
