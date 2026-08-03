-- State machines are enforced in the application. These constraints protect the data when a
-- migration, integration, or ad-hoc query bypasses the application service layer.

-- IN_PROGRESS and COMPLETED were execution states before work orders were separated in V8.
-- Their execution history has already been preserved in work_orders, so their booking is confirmed.
UPDATE appointments
SET status = 'CONFIRMED'
WHERE status IN ('IN_PROGRESS', 'COMPLETED');

UPDATE appointments
SET status = 'PENDING'
WHERE status IS NULL OR status NOT IN ('PENDING', 'CONFIRMED', 'CANCELLED');

UPDATE work_orders
SET status = 'OPEN'
WHERE status IS NULL
   OR status NOT IN ('OPEN', 'DIAGNOSING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');

UPDATE payments
SET payment_status = 'UNPAID'
WHERE payment_status IS NULL
   OR payment_status NOT IN ('UNPAID', 'PAID', 'FAILED', 'REFUNDED');

UPDATE users
SET status = 'ACTIVE'
WHERE status IS NULL
   OR status NOT IN ('PENDING_VERIFICATION', 'ACTIVE', 'INACTIVE');

UPDATE services
SET status = 'ACTIVE'
WHERE status IS NULL OR status NOT IN ('ACTIVE', 'INACTIVE');

ALTER TABLE appointments DROP CONSTRAINT IF EXISTS chk_appointments_status;
ALTER TABLE appointments
    ADD CONSTRAINT chk_appointments_status
    CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED'));

ALTER TABLE work_orders DROP CONSTRAINT IF EXISTS chk_work_orders_status;
ALTER TABLE work_orders
    ADD CONSTRAINT chk_work_orders_status
    CHECK (status IN ('OPEN', 'DIAGNOSING', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'));

ALTER TABLE payments DROP CONSTRAINT IF EXISTS chk_payments_payment_status;
ALTER TABLE payments
    ADD CONSTRAINT chk_payments_payment_status
    CHECK (payment_status IN ('UNPAID', 'PAID', 'FAILED', 'REFUNDED'));

ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_users_status;
ALTER TABLE users
    ADD CONSTRAINT chk_users_status
    CHECK (status IN ('PENDING_VERIFICATION', 'ACTIVE', 'INACTIVE'));

ALTER TABLE services DROP CONSTRAINT IF EXISTS chk_services_status;
ALTER TABLE services
    ADD CONSTRAINT chk_services_status
    CHECK (status IN ('ACTIVE', 'INACTIVE'));
