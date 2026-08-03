-- Appointments represent customer bookings. Work orders represent workshop execution.
CREATE TABLE work_orders (
    id VARCHAR(64) PRIMARY KEY,
    appointment_id VARCHAR(64) UNIQUE REFERENCES appointments(id) ON DELETE SET NULL,
    customer_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    vehicle_id VARCHAR(64) NOT NULL REFERENCES vehicles(id) ON DELETE RESTRICT,
    service_id VARCHAR(64) NOT NULL REFERENCES services(id) ON DELETE RESTRICT,
    technician_id VARCHAR(64) REFERENCES users(id) ON DELETE SET NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'OPEN',
    intake_notes TEXT,
    diagnostic_notes TEXT,
    opened_at TIMESTAMP WITH TIME ZONE,
    started_at TIMESTAMP WITH TIME ZONE,
    completed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_work_orders_customer_id ON work_orders(customer_id);
CREATE INDEX idx_work_orders_technician_id ON work_orders(technician_id);
CREATE INDEX idx_work_orders_status ON work_orders(status);

-- Preserve execution history that was previously stored on appointments.
INSERT INTO work_orders (
    id, appointment_id, customer_id, vehicle_id, service_id, technician_id, status,
    intake_notes, opened_at, started_at, completed_at, created_at, updated_at
)
SELECT
    'WO-' || substring(a.id FROM 5),
    a.id,
    a.customer_id,
    a.vehicle_id,
    a.service_id,
    a.technician_id,
    a.status,
    a.notes,
    a.created_at,
    CASE WHEN a.status IN ('IN_PROGRESS', 'COMPLETED') THEN a.updated_at END,
    CASE WHEN a.status = 'COMPLETED' THEN a.updated_at END,
    a.created_at,
    a.updated_at
FROM appointments a
WHERE a.status IN ('IN_PROGRESS', 'COMPLETED')
ON CONFLICT (appointment_id) DO NOTHING;

ALTER TABLE payments ADD COLUMN IF NOT EXISTS work_order_id VARCHAR(64);
ALTER TABLE feedbacks ADD COLUMN IF NOT EXISTS work_order_id VARCHAR(64);
ALTER TABLE payments ALTER COLUMN appointment_id DROP NOT NULL;
ALTER TABLE feedbacks ALTER COLUMN appointment_id DROP NOT NULL;

UPDATE payments p
SET work_order_id = wo.id
FROM work_orders wo
WHERE p.appointment_id = wo.appointment_id
  AND p.work_order_id IS NULL;

UPDATE feedbacks f
SET work_order_id = wo.id
FROM work_orders wo
WHERE f.appointment_id = wo.appointment_id
  AND f.work_order_id IS NULL;

ALTER TABLE payments
    ADD CONSTRAINT fk_payments_work_order
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE RESTRICT;
ALTER TABLE feedbacks
    ADD CONSTRAINT fk_feedbacks_work_order
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE RESTRICT;

CREATE UNIQUE INDEX uq_payments_work_order_id
    ON payments(work_order_id) WHERE work_order_id IS NOT NULL;
CREATE INDEX idx_feedbacks_work_order_id ON feedbacks(work_order_id);
