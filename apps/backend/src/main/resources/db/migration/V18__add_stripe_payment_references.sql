ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS stripe_checkout_session_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS stripe_payment_intent_id VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS uq_payments_stripe_checkout_session_id
    ON payments(stripe_checkout_session_id)
    WHERE stripe_checkout_session_id IS NOT NULL;
