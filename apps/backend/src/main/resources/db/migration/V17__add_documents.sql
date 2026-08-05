CREATE TABLE documents (
    id VARCHAR(64) PRIMARY KEY,
    work_order_id VARCHAR(64) NOT NULL REFERENCES work_orders(id) ON DELETE CASCADE,
    uploaded_by VARCHAR(64) REFERENCES users(id) ON DELETE SET NULL,
    document_type VARCHAR(32) NOT NULL CHECK (document_type IN (
        'VEHICLE_CONDITION', 'DIAGNOSTIC_EVIDENCE', 'REPAIR_EVIDENCE', 'REPORT', 'INVOICE', 'OTHER'
    )),
    file_name VARCHAR(255) NOT NULL,
    storage_key VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL CHECK (content_type IN (
        'image/jpeg', 'image/png', 'image/webp', 'application/pdf'
    )),
    size_bytes BIGINT NOT NULL CHECK (size_bytes BETWEEN 1 AND 10485760),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_documents_work_order_created_at ON documents (work_order_id, created_at DESC);
