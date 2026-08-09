package com.apu.asc.payment.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stripe is unauthenticated at the HTTP layer; its signed webhook is verified before processing.
 */
@RestController
@RequestMapping("/api/v1/stripe")
@RequiredArgsConstructor
@Tag(name = "Stripe", description = "Signed Stripe webhook endpoints")
class StripeWebhookController {

  private final StripeCheckoutService stripeCheckoutService;

  @PostMapping("/webhook")
  @Operation(summary = "Receive a signed Stripe Checkout event")
  public ResponseEntity<Void> receiveWebhook(
      @RequestBody byte[] payload,
      @RequestHeader(value = "Stripe-Signature", required = false) String signature) {
    stripeCheckoutService.handleWebhook(payload, signature);
    return ResponseEntity.ok().build();
  }
}
