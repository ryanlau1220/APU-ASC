package com.apu.asc.payment.internal;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.payment.PaymentApi;
import com.apu.asc.payment.PaymentDto;
import com.apu.asc.payment.PaymentStatus;
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
  public PaymentDto getByWorkOrder(final String workOrderId) {
    return paymentRepository
        .findByWorkOrderId(workOrderId)
        .map(this::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("Payment for work order", workOrderId));
  }

  @Override
  @Transactional(readOnly = true)
  public List<PaymentDto> getByCustomer(final String customerId) {
    return paymentRepository.findByCustomerId(customerId).stream().map(this::toDto).toList();
  }

  @Override
  @Transactional
  public PaymentDto createInvoice(final PaymentDto paymentDto) {
    if (paymentDto.paymentStatus() != null
        && !paymentDto.paymentStatus().isBlank()
        && PaymentStatus.fromString(paymentDto.paymentStatus()) != PaymentStatus.UNPAID) {
      throw new IllegalArgumentException("New invoices must start in UNPAID status.");
    }
    String id = paymentDto.id() != null ? paymentDto.id() : "PAY-" + UUID.randomUUID().toString();
    String invoiceNum =
        paymentDto.invoiceNumber() != null
            ? paymentDto.invoiceNumber()
            : "INV-" + System.currentTimeMillis();
    PaymentEntity entity =
        PaymentEntity.builder()
            .id(id)
            .appointmentId(paymentDto.appointmentId())
            .workOrderId(paymentDto.workOrderId())
            .customerId(paymentDto.customerId())
            .invoiceNumber(invoiceNum)
            .amount(paymentDto.amount())
            .paymentMethod(
                paymentDto.paymentMethod() != null && !paymentDto.paymentMethod().isBlank()
                    ? paymentDto.paymentMethod()
                    : "PENDING")
            .paymentStatus(PaymentStatus.UNPAID.name())
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
    if (paymentDto.paymentStatus() != null
        && PaymentStatus.fromString(paymentDto.paymentStatus())
            != PaymentStatus.fromString(entity.getPaymentStatus())) {
      throw new IllegalArgumentException(
          "Payment status can only be changed by a payment or refund operation.");
    }

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
    if (method == null || method.isBlank()) {
      throw new IllegalArgumentException("A payment method is required.");
    }
    PaymentStatus current = PaymentStatus.fromString(payment.getPaymentStatus());
    if (current == PaymentStatus.PAID) {
      return toDto(payment);
    }
    current.requireTransitionTo(PaymentStatus.PAID);
    payment.setPaymentMethod(method);
    payment.setPaymentStatus(PaymentStatus.PAID.name());
    if (payment.getPaidAt() == null) payment.setPaidAt(Instant.now());
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
        entity.getCreatedAt(),
        entity.getWorkOrderId());
  }
}
