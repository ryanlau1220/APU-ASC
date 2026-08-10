package com.apu.asc.payment.internal;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.common.event.LiveUpdateEvent;
import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.notification.NotificationRequestedEvent;
import com.apu.asc.notification.NotificationType;
import com.apu.asc.payment.PaymentApi;
import com.apu.asc.payment.PaymentDto;
import com.apu.asc.payment.PaymentMethod;
import com.apu.asc.payment.PaymentRecordDto;
import com.apu.asc.payment.PaymentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Set;
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
            .paymentMethod(PaymentMethod.PENDING.name())
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
    PaymentMethod paymentMethod = PaymentMethod.fromString(method);
    if (paymentMethod != PaymentMethod.CASH
        && paymentMethod != PaymentMethod.CREDIT_CARD
        && paymentMethod != PaymentMethod.DEBIT_CARD) {
      throw new IllegalArgumentException(
          "Only counter cash or card payments can be recorded manually.");
    }
    String beforeState = paymentAuditState(payment);
    current.requireTransitionTo(PaymentStatus.PAID);
    payment.setPaymentMethod(paymentMethod.name());
    payment.setPaymentStatus(PaymentStatus.PAID.name());
    if (payment.getPaidAt() == null) payment.setPaidAt(Instant.now());
    PaymentDto updated = toDto(paymentRepository.save(payment));
    eventPublisher.publishEvent(
        new AuditEvent(
            updated.customerId(),
            "PAYMENT_PROCESSED",
            "PAYMENT",
            updated.id(),
            "Processed payment.",
            beforeState,
            paymentAuditState(payment)));
    publishPaymentUpdates(updated);
    return updated;
  }

  @Override
  @Transactional
  public PaymentDto voidInvoice(final String paymentId, final String reason) {
    PaymentEntity payment = requirePayment(paymentId);
    PaymentStatus current = PaymentStatus.fromString(payment.getPaymentStatus());
    current.requireTransitionTo(PaymentStatus.VOID);
    String beforeState = paymentAuditState(payment);
    payment.setPaymentStatus(PaymentStatus.VOID.name());
    payment.setVoidedAt(Instant.now());
    payment.setVoidReason(reason.trim());
    PaymentDto updated = toDto(paymentRepository.save(payment));
    eventPublisher.publishEvent(
        new AuditEvent(
            updated.customerId(),
            "INVOICE_VOIDED",
            "PAYMENT",
            updated.id(),
            "Voided invoice " + updated.invoiceNumber(),
            beforeState,
            paymentAuditState(payment)));
    publishPaymentLiveUpdate(updated);
    return updated;
  }

  @Override
  @Transactional
  public PaymentDto refundCounterPayment(final String paymentId, final String reason) {
    PaymentEntity payment = requirePayment(paymentId);
    if (PaymentMethod.STRIPE_CHECKOUT.name().equals(payment.getPaymentMethod())) {
      throw new IllegalArgumentException("Stripe payments must be refunded through Stripe.");
    }
    PaymentStatus current = PaymentStatus.fromString(payment.getPaymentStatus());
    current.requireTransitionTo(PaymentStatus.REFUNDED);
    String beforeState = paymentAuditState(payment);
    payment.setPaymentStatus(PaymentStatus.REFUNDED.name());
    payment.setRefundedAt(Instant.now());
    payment.setRefundReason(reason.trim());
    PaymentDto updated = toDto(paymentRepository.save(payment));
    eventPublisher.publishEvent(
        new AuditEvent(
            updated.customerId(),
            "COUNTER_PAYMENT_REFUNDED",
            "PAYMENT",
            updated.id(),
            "Recorded counter refund for invoice " + updated.invoiceNumber(),
            beforeState,
            paymentAuditState(payment)));
    publishPaymentRefunded(updated);
    return updated;
  }

  @Override
  @Transactional(readOnly = true)
  public PaymentRecordDto getPaymentRecord(final String paymentId) {
    return paymentRepository
        .findById(paymentId)
        .map(this::toRecord)
        .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
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

  private PaymentRecordDto toRecord(PaymentEntity entity) {
    return new PaymentRecordDto(
        entity.getId(),
        entity.getInvoiceNumber(),
        entity.getAmount(),
        entity.getPaymentMethod(),
        entity.getPaymentStatus(),
        entity.getPaidAt(),
        entity.getVoidedAt(),
        entity.getVoidReason(),
        entity.getRefundedAt(),
        entity.getRefundReason(),
        entity.getCreatedAt());
  }

  private PaymentEntity requirePayment(String paymentId) {
    return paymentRepository
        .findById(paymentId)
        .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
  }

  private String paymentAuditState(PaymentEntity entity) {
    return "amount="
        + entity.getAmount()
        + "; paymentMethod="
        + entity.getPaymentMethod()
        + "; paymentStatus="
        + entity.getPaymentStatus();
  }

  private void publishPaymentUpdates(PaymentDto payment) {
    publishPaymentLiveUpdate(payment);
    eventPublisher.publishEvent(
        NotificationRequestedEvent.forUsers(
            NotificationType.PAYMENT_RECEIVED,
            Set.of(payment.customerId()),
            "Payment received",
            "Payment for invoice " + payment.invoiceNumber() + " has been received.",
            "/customer/payments"));
  }

  private void publishPaymentRefunded(PaymentDto payment) {
    publishPaymentLiveUpdate(payment);
    eventPublisher.publishEvent(
        NotificationRequestedEvent.forUsers(
            NotificationType.PAYMENT_REFUNDED,
            Set.of(payment.customerId()),
            "Payment refunded",
            "A refund has been recorded for invoice " + payment.invoiceNumber() + ".",
            "/customer/payments"));
  }

  private void publishPaymentLiveUpdate(PaymentDto payment) {
    eventPublisher.publishEvent(
        LiveUpdateEvent.forUsersAndRoles(
            "payments", payment.id(), Set.of(payment.customerId()), Set.of("STAFF", "MANAGER")));
  }
}
