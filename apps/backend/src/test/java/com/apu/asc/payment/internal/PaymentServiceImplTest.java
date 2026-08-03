package com.apu.asc.payment.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.apu.asc.common.exception.ResourceNotFoundException;
import com.apu.asc.payment.PaymentDto;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private PaymentServiceImpl paymentService;

  @BeforeEach
  void setUp() {
    paymentService = new PaymentServiceImpl(paymentRepository, eventPublisher);
  }

  @Test
  @DisplayName("Should return all payments")
  void shouldReturnAllPayments() {
    PaymentEntity payment =
        PaymentEntity.builder()
            .id("PAY-1")
            .appointmentId("APT-1")
            .customerId("CUST-1")
            .invoiceNumber("INV-001")
            .amount(BigDecimal.valueOf(150.00))
            .paymentMethod("CREDIT_CARD")
            .paymentStatus("PAID")
            .createdAt(Instant.now())
            .build();

    when(paymentRepository.findAll()).thenReturn(List.of(payment));

    List<PaymentDto> results = paymentService.findAllPayments();

    assertThat(results).hasSize(1);
    assertThat(results.get(0).invoiceNumber()).isEqualTo("INV-001");
  }

  @Test
  @DisplayName("Should process payment and update status to PAID")
  void shouldProcessPayment() {
    PaymentEntity payment =
        PaymentEntity.builder()
            .id("PAY-1")
            .appointmentId("APT-1")
            .customerId("CUST-1")
            .invoiceNumber("INV-001")
            .amount(BigDecimal.valueOf(150.00))
            .paymentMethod("CREDIT_CARD")
            .paymentStatus("UNPAID")
            .createdAt(Instant.now())
            .build();

    when(paymentRepository.findById("PAY-1")).thenReturn(Optional.of(payment));
    when(paymentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    PaymentDto updated = paymentService.processPayment("PAY-1", "CREDIT_CARD");

    assertThat(updated.paymentStatus()).isEqualTo("PAID");
    assertThat(updated.paidAt()).isNotNull();
    verify(paymentRepository).save(any());
  }

  @Test
  @DisplayName("Should throw ResourceNotFoundException when payment not found")
  void shouldThrowExceptionWhenNotFound() {
    when(paymentRepository.findById("INVALID-ID")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> paymentService.getPaymentById("INVALID-ID"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @DisplayName("Should keep a payment idempotent after it is paid")
  void shouldNotProcessAnAlreadyPaidPaymentAgain() {
    PaymentEntity payment =
        PaymentEntity.builder()
            .id("PAY-1")
            .customerId("CUST-1")
            .invoiceNumber("INV-001")
            .amount(BigDecimal.valueOf(150.00))
            .paymentMethod("CREDIT_CARD")
            .paymentStatus("PAID")
            .paidAt(Instant.now())
            .build();
    when(paymentRepository.findById("PAY-1")).thenReturn(Optional.of(payment));

    PaymentDto updated = paymentService.processPayment("PAY-1", "BANK_TRANSFER");

    assertThat(updated.paymentMethod()).isEqualTo("CREDIT_CARD");
    verifyNoInteractions(eventPublisher);
  }
}
