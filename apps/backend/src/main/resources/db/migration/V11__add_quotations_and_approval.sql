CREATE TABLE quotations (
    id VARCHAR(64) PRIMARY KEY,
    quote_number VARCHAR(64) NOT NULL UNIQUE,
    work_order_id VARCHAR(64) NOT NULL REFERENCES work_orders(id) ON DELETE RESTRICT,
    customer_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    revision INTEGER NOT NULL CHECK (revision > 0),
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT'
        CHECK (status IN ('DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'REJECTED', 'EXPIRED')),
    notes TEXT,
    response_notes TEXT,
    valid_until DATE NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL CHECK (subtotal >= 0),
    tax_amount NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (tax_amount >= 0),
    total_amount NUMERIC(12, 2) NOT NULL CHECK (total_amount >= 0),
    submitted_at TIMESTAMP WITH TIME ZONE,
    responded_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE quotation_items (
    id VARCHAR(64) PRIMARY KEY,
    quotation_id VARCHAR(64) NOT NULL REFERENCES quotations(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL CHECK (unit_price >= 0),
    line_total NUMERIC(12, 2) NOT NULL CHECK (line_total >= 0)
);

CREATE INDEX idx_quotations_customer_created ON quotations(customer_id, created_at DESC);
CREATE INDEX idx_quotations_work_order_revision ON quotations(work_order_id, revision DESC);
CREATE INDEX idx_quotation_items_quotation_id ON quotation_items(quotation_id);

-- Only one estimate may be actively edited or awaiting the customer's answer for a work order.
CREATE UNIQUE INDEX uq_quotations_outstanding_work_order
    ON quotations(work_order_id)
    WHERE status IN ('DRAFT', 'PENDING_APPROVAL');

ALTER TABLE work_orders
    ADD COLUMN approved_quotation_id VARCHAR(64),
    ADD COLUMN quotation_approved_at TIMESTAMP WITH TIME ZONE;
