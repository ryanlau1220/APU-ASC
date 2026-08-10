ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS stripe_refund_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS voided_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS void_reason VARCHAR(500),
    ADD COLUMN IF NOT EXISTS refunded_at TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS refund_reason VARCHAR(500);

CREATE UNIQUE INDEX IF NOT EXISTS uq_payments_stripe_refund_id
    ON payments(stripe_refund_id)
    WHERE stripe_refund_id IS NOT NULL;

ALTER TABLE payments DROP CONSTRAINT IF EXISTS chk_payments_payment_status;
ALTER TABLE payments
    ADD CONSTRAINT chk_payments_payment_status
    CHECK (payment_status IN ('UNPAID', 'PAID', 'FAILED', 'VOID', 'REFUND_PENDING', 'REFUNDED'));
