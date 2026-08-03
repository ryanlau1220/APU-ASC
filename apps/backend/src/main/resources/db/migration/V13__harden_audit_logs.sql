ALTER TABLE audit_logs
    ADD COLUMN actor_id VARCHAR(128),
    ADD COLUMN actor_username VARCHAR(128),
    ADD COLUMN actor_role VARCHAR(64),
    ADD COLUMN correlation_id VARCHAR(128),
    ADD COLUMN request_method VARCHAR(16),
    ADD COLUMN request_path VARCHAR(512),
    ADD COLUMN client_ip VARCHAR(64),
    ADD COLUMN user_agent VARCHAR(512),
    ADD COLUMN before_state TEXT,
    ADD COLUMN after_state TEXT;

CREATE INDEX idx_audit_logs_actor_id ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_correlation_id ON audit_logs(correlation_id);

CREATE OR REPLACE FUNCTION prevent_audit_log_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit logs are append-only and cannot be modified or deleted';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_logs_append_only
    BEFORE UPDATE OR DELETE ON audit_logs
    FOR EACH ROW EXECUTE FUNCTION prevent_audit_log_mutation();
