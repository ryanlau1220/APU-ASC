package com.apu.asc.payment;

/** A short-lived Stripe-hosted checkout URL for one existing invoice. */
public record CheckoutSessionDto(String checkoutUrl) {}
