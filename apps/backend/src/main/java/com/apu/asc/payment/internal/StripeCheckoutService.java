package com.apu.asc.payment.internal;

import com.apu.asc.common.event.AuditEvent;
import com.apu.asc.common.event.LiveUpdateEvent;
import com.apu.asc.notification.NotificationRequestedEvent;
import com.apu.asc.notification.NotificationType;
import com.apu.asc.payment.CheckoutSessionDto;
import com.apu.asc.payment.PaymentMethod;
import com.apu.asc.payment.PaymentStatus;
import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Owns the provider boundary for hosted Stripe Checkout and signed webhook settlement. */
@Service
@RequiredArgsConstructor
@Slf4j
class StripeCheckoutService {

  private static final String PAYMENT_STATUS_PAID = "paid";

  private final PaymentRepository paymentRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Value("${stripe.secret-key:}")
  private String secretKey;

  @Value("${stripe.webhook-secret:}")
  private String webhookSecret;

  @Value("${stripe.currency:myr}")
  private String currency;

  @Value("${app.frontend-base-url:http://localhost:3000}")
  private String frontendBaseUrl;

  @Transactional
  CheckoutSessionDto createCheckoutSession(String paymentId) {
    PaymentEntity payment =
        paymentRepository
            .findById(paymentId)
            .orElseThrow(
                () ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "The requested invoice does not exist."));
    PaymentStatus status = PaymentStatus.fromString(payment.getPaymentStatus());
    if (status == PaymentStatus.PAID) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "This invoice has already been paid.");
    }
    if (status == PaymentStatus.REFUNDED) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "A refunded invoice cannot be paid again.");
    }

    StripeClient stripeClient = stripeClient();
    CheckoutSessionDto existingSession = findOpenCheckoutSession(payment, stripeClient);
    if (existingSession != null) {
      return existingSession;
    }

    try {
      Session session = stripeClient.v1().checkout().sessions().create(createParams(payment));
      if (session.getId() == null || session.getUrl() == null) {
        throw new ResponseStatusException(
            HttpStatus.BAD_GATEWAY, "Stripe Checkout did not return a checkout URL.");
      }
      payment.setStripeCheckoutSessionId(session.getId());
      payment.setPaymentMethod(PaymentMethod.STRIPE_CHECKOUT.name());
      paymentRepository.save(payment);
      eventPublisher.publishEvent(
          new AuditEvent(
              payment.getCustomerId(),
              "STRIPE_CHECKOUT_CREATED",
              "PAYMENT",
              payment.getId(),
              "Created a Stripe Checkout session for invoice " + payment.getInvoiceNumber()));
      return new CheckoutSessionDto(session.getUrl());
    } catch (StripeException ex) {
      log.warn("Stripe Checkout creation failed for payment {}", paymentId, ex);
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Unable to start online checkout.");
    }
  }

  @Transactional
  void handleWebhook(byte[] payload, String signature) {
    requireConfigured(webhookSecret, "Stripe webhooks are not configured.");
    if (signature == null || signature.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing Stripe signature.");
    }

    final Event event;
    try {
      event =
          Webhook.constructEvent(
              new String(payload, StandardCharsets.UTF_8), signature, webhookSecret);
    } catch (SignatureVerificationException ex) {
      log.warn("Rejected Stripe webhook with an invalid signature");
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Stripe signature.");
    }

    if (!isCheckoutSettlementEvent(event.getType())) {
      log.debug("Ignored Stripe event type {}", event.getType());
      return;
    }

    Optional<StripeObject> object = deserializeCheckoutSession(event);
    if (object.isEmpty() || !(object.get() instanceof Session session)) {
      log.warn(
          "Ignored Stripe event {} because it did not contain a Checkout Session", event.getType());
      return;
    }

    switch (event.getType()) {
      case "checkout.session.completed", "checkout.session.async_payment_succeeded" ->
          settleCompletedSession(session);
      case "checkout.session.async_payment_failed", "checkout.session.expired" ->
          markFailedSession(session);
      default -> {
        // isCheckoutSettlementEvent above limits this switch to the supported event types.
      }
    }
  }

  private boolean isCheckoutSettlementEvent(String eventType) {
    return Set.of(
            "checkout.session.completed",
            "checkout.session.async_payment_succeeded",
            "checkout.session.async_payment_failed",
            "checkout.session.expired")
        .contains(eventType);
  }

  private Optional<StripeObject> deserializeCheckoutSession(Event event) {
    Optional<StripeObject> compatibleObject = event.getDataObjectDeserializer().getObject();
    if (compatibleObject.isPresent()) {
      return compatibleObject;
    }
    try {
      // The Stripe signature has already been verified. Events retain the account's API version,
      // which can differ from the SDK version and makes the compatibility-safe path unavailable.
      return Optional.of(event.getDataObjectDeserializer().deserializeUnsafe());
    } catch (StripeException ex) {
      log.warn("Could not deserialize signed Stripe event {}", event.getId(), ex);
      return Optional.empty();
    }
  }

  private CheckoutSessionDto findOpenCheckoutSession(
      PaymentEntity payment, StripeClient stripeClient) {
    if (payment.getStripeCheckoutSessionId() == null) {
      return null;
    }
    try {
      Session existing =
          stripeClient.v1().checkout().sessions().retrieve(payment.getStripeCheckoutSessionId());
      if ("open".equals(existing.getStatus()) && existing.getUrl() != null) {
        return new CheckoutSessionDto(existing.getUrl());
      }
    } catch (StripeException ex) {
      log.info(
          "Existing Stripe Checkout session could not be reused for payment {}", payment.getId());
    }
    return null;
  }

  private SessionCreateParams createParams(PaymentEntity payment) {
    BigDecimal amount = payment.getAmount();
    if (amount == null || amount.signum() <= 0) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "This invoice has an invalid amount.");
    }
    long amountInMinorUnits;
    try {
      amountInMinorUnits =
          amount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
    } catch (ArithmeticException ex) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "This invoice amount is not payable online.");
    }
    String normalizedCurrency = currency.trim().toLowerCase();
    if (normalizedCurrency.length() != 3) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE, "Online payments are misconfigured.");
    }

    SessionCreateParams.LineItem.PriceData.ProductData product =
        SessionCreateParams.LineItem.PriceData.ProductData.builder()
            .setName("APU-ASC invoice " + payment.getInvoiceNumber())
            .build();
    SessionCreateParams.LineItem.PriceData priceData =
        SessionCreateParams.LineItem.PriceData.builder()
            .setCurrency(normalizedCurrency)
            .setUnitAmount(amountInMinorUnits)
            .setProductData(product)
            .build();
    SessionCreateParams.LineItem lineItem =
        SessionCreateParams.LineItem.builder().setQuantity(1L).setPriceData(priceData).build();
    String paymentsPage = frontendBaseUrl + "/customer/payments";
    return SessionCreateParams.builder()
        .setMode(SessionCreateParams.Mode.PAYMENT)
        .setClientReferenceId(payment.getId())
        .putMetadata("paymentId", payment.getId())
        .setSuccessUrl(paymentsPage + "?checkout=success")
        .setCancelUrl(paymentsPage + "?checkout=cancelled")
        .addLineItem(lineItem)
        .build();
  }

  private void settleCompletedSession(Session session) {
    if (!PAYMENT_STATUS_PAID.equals(session.getPaymentStatus())) {
      log.info("Stripe Checkout session {} completed without a settled payment", session.getId());
      return;
    }
    PaymentEntity payment = findMatchingPayment(session);
    if (payment == null
        || PaymentStatus.fromString(payment.getPaymentStatus()) == PaymentStatus.PAID) {
      return;
    }
    PaymentStatus current = PaymentStatus.fromString(payment.getPaymentStatus());
    current.requireTransitionTo(PaymentStatus.PAID);
    String beforeState = paymentAuditState(payment);
    payment.setPaymentMethod(PaymentMethod.STRIPE_CHECKOUT.name());
    payment.setPaymentStatus(PaymentStatus.PAID.name());
    payment.setStripePaymentIntentId(session.getPaymentIntent());
    payment.setPaidAt(Instant.now());
    PaymentEntity saved = paymentRepository.save(payment);
    eventPublisher.publishEvent(
        new AuditEvent(
            null,
            "STRIPE_PAYMENT_CONFIRMED",
            "PAYMENT",
            saved.getId(),
            "Stripe confirmed payment for invoice " + saved.getInvoiceNumber(),
            beforeState,
            paymentAuditState(saved)));
    publishPaymentUpdates(saved);
  }

  private void markFailedSession(Session session) {
    PaymentEntity payment = findMatchingPayment(session);
    if (payment == null
        || PaymentStatus.fromString(payment.getPaymentStatus()) == PaymentStatus.PAID) {
      return;
    }
    PaymentStatus current = PaymentStatus.fromString(payment.getPaymentStatus());
    if (current != PaymentStatus.FAILED) {
      current.requireTransitionTo(PaymentStatus.FAILED);
      payment.setPaymentStatus(PaymentStatus.FAILED.name());
      paymentRepository.save(payment);
      eventPublisher.publishEvent(
          new AuditEvent(
              null,
              "STRIPE_PAYMENT_FAILED",
              "PAYMENT",
              payment.getId(),
              "Stripe Checkout did not complete payment for invoice "
                  + payment.getInvoiceNumber()));
      eventPublisher.publishEvent(
          LiveUpdateEvent.forUsersAndRoles(
              "payments",
              payment.getId(),
              Set.of(payment.getCustomerId()),
              Set.of("STAFF", "MANAGER")));
    }
  }

  private PaymentEntity findMatchingPayment(Session session) {
    if (session.getId() == null) {
      log.warn("Ignored Stripe Checkout session without an ID");
      return null;
    }
    Optional<PaymentEntity> payment =
        paymentRepository.findByStripeCheckoutSessionId(session.getId());
    if (payment.isEmpty()
        || !payment.get().getId().equals(session.getClientReferenceId())
        || !matchesInvoice(session, payment.get())) {
      log.error(
          "Stripe Checkout session {} does not match a payable APU-ASC invoice", session.getId());
      return null;
    }
    return payment.get();
  }

  private boolean matchesInvoice(Session session, PaymentEntity payment) {
    if (session.getAmountTotal() == null || session.getCurrency() == null) {
      return false;
    }
    try {
      long expectedAmount =
          payment
              .getAmount()
              .setScale(2, RoundingMode.UNNECESSARY)
              .movePointRight(2)
              .longValueExact();
      return expectedAmount == session.getAmountTotal()
          && currency.trim().equalsIgnoreCase(session.getCurrency());
    } catch (ArithmeticException ex) {
      return false;
    }
  }

  private StripeClient stripeClient() {
    requireConfigured(secretKey, "Online payments are not configured.");
    return new StripeClient(secretKey);
  }

  private void requireConfigured(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, message);
    }
  }

  private String paymentAuditState(PaymentEntity payment) {
    return "amount="
        + payment.getAmount()
        + "; paymentMethod="
        + payment.getPaymentMethod()
        + "; paymentStatus="
        + payment.getPaymentStatus();
  }

  private void publishPaymentUpdates(PaymentEntity payment) {
    eventPublisher.publishEvent(
        LiveUpdateEvent.forUsersAndRoles(
            "payments",
            payment.getId(),
            Set.of(payment.getCustomerId()),
            Set.of("STAFF", "MANAGER")));
    eventPublisher.publishEvent(
        NotificationRequestedEvent.forUsers(
            NotificationType.PAYMENT_RECEIVED,
            Set.of(payment.getCustomerId()),
            "Payment received",
            "Payment for invoice " + payment.getInvoiceNumber() + " has been received.",
            "/customer/payments"));
  }
}
