package com.apu.asc.payment.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class StripeCheckoutServiceTest {

  private static final String WEBHOOK_SECRET = "whsec_test_only";

  @Mock private PaymentRepository paymentRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private StripeCheckoutService stripeCheckoutService;

  @BeforeEach
  void setUp() {
    stripeCheckoutService = new StripeCheckoutService(paymentRepository, eventPublisher);
    ReflectionTestUtils.setField(stripeCheckoutService, "webhookSecret", WEBHOOK_SECRET);
    ReflectionTestUtils.setField(stripeCheckoutService, "currency", "myr");
  }

  @Test
  void settlesOnlyAValidSignedCheckoutCompletionForTheMatchingInvoice() throws Exception {
    PaymentEntity payment =
        PaymentEntity.builder()
            .id("PAY-1")
            .customerId("CUS-1")
            .invoiceNumber("INV-1")
            .amount(new BigDecimal("12.50"))
            .paymentMethod("STRIPE_CHECKOUT")
            .paymentStatus("UNPAID")
            .stripeCheckoutSessionId("cs_test_1")
            .build();
    byte[] payload = completedCheckoutPayload();
    when(paymentRepository.findByStripeCheckoutSessionId("cs_test_1"))
        .thenReturn(Optional.of(payment));
    when(paymentRepository.save(payment)).thenReturn(payment);

    stripeCheckoutService.handleWebhook(payload, validSignature(payload));

    assertThat(payment.getPaymentStatus()).isEqualTo("PAID");
    assertThat(payment.getStripePaymentIntentId()).isEqualTo("pi_test_1");
    assertThat(payment.getPaidAt()).isNotNull();
    verify(paymentRepository).save(payment);
    verify(eventPublisher, times(3)).publishEvent(any(Object.class));
  }

  @Test
  void rejectsAnUnsignedOrTamperedWebhookBeforeItCanChangeAPayment() {
    byte[] payload = completedCheckoutPayload();

    assertThatThrownBy(
            () -> stripeCheckoutService.handleWebhook(payload, "t=1,v1=not-a-valid-signature"))
        .isInstanceOf(ResponseStatusException.class)
        .extracting(error -> ((ResponseStatusException) error).getStatusCode())
        .isEqualTo(HttpStatus.BAD_REQUEST);

    verify(paymentRepository, never()).findByStripeCheckoutSessionId(any());
    verify(paymentRepository, never()).save(any());
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  void confirmsARefundOnlyFromTheMatchingSignedStripeWebhook() throws Exception {
    PaymentEntity payment =
        PaymentEntity.builder()
            .id("PAY-1")
            .customerId("CUS-1")
            .invoiceNumber("INV-1")
            .amount(new BigDecimal("12.50"))
            .paymentMethod("STRIPE_CHECKOUT")
            .paymentStatus("REFUND_PENDING")
            .stripePaymentIntentId("pi_test_1")
            .stripeRefundId("re_test_1")
            .refundReason("Customer request")
            .build();
    byte[] payload = completedRefundPayload();
    when(paymentRepository.findByStripeRefundId("re_test_1")).thenReturn(Optional.of(payment));
    when(paymentRepository.save(payment)).thenReturn(payment);

    stripeCheckoutService.handleWebhook(payload, validSignature(payload));

    assertThat(payment.getPaymentStatus()).isEqualTo("REFUNDED");
    assertThat(payment.getRefundedAt()).isNotNull();
    verify(paymentRepository).save(payment);
    verify(eventPublisher, times(3)).publishEvent(any(Object.class));
  }

  private byte[] completedCheckoutPayload() {
    return """
        {
          "id": "evt_test_1",
          "object": "event",
          "api_version": "2025-01-27.acacia",
          "type": "checkout.session.completed",
          "data": {
            "object": {
              "id": "cs_test_1",
              "object": "checkout.session",
              "payment_status": "paid",
              "amount_total": 1250,
              "currency": "myr",
              "client_reference_id": "PAY-1",
              "payment_intent": "pi_test_1"
            }
          }
        }
        """
        .getBytes(StandardCharsets.UTF_8);
  }

  private byte[] completedRefundPayload() {
    return """
        {
          "id": "evt_test_refund_1",
          "object": "event",
          "api_version": "2025-01-27.acacia",
          "type": "refund.updated",
          "data": {
            "object": {
              "id": "re_test_1",
              "object": "refund",
              "status": "succeeded",
              "payment_intent": "pi_test_1"
            }
          }
        }
        """
        .getBytes(StandardCharsets.UTF_8);
  }

  private String validSignature(byte[] payload) throws Exception {
    long timestamp = Instant.now().getEpochSecond();
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] signature =
        mac.doFinal(
            (timestamp + "." + new String(payload, StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8));
    return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(signature);
  }
}
