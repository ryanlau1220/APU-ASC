-- Migration: V4__audit_logs_partitioning.sql
-- Description: Convert audit_logs table to Range Partitioning by month with indexing

-- 1. Drop existing unpartitioned table and foreign keys if any
ALTER TABLE IF EXISTS audit_logs DROP CONSTRAINT IF EXISTS audit_logs_user_id_fkey;
CREATE TABLE IF NOT EXISTS audit_logs_backup AS SELECT * FROM audit_logs;
DROP TABLE IF EXISTS audit_logs CASCADE;

-- 2. Create partitioned parent table without strict FK locks to allow system and post-deletion compliance logs
CREATE TABLE audit_logs (
    id VARCHAR(50) NOT NULL,
    user_id VARCHAR(50),
    action_type VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id, created_at)
) PARTITION BY RANGE (created_at);

-- 3. Create monthly partitions for current and upcoming periods
CREATE TABLE audit_logs_2026_07 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-07-01 00:00:00+00') TO ('2026-08-01 00:00:00+00');

CREATE TABLE audit_logs_2026_08 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-08-01 00:00:00+00') TO ('2026-09-01 00:00:00+00');

CREATE TABLE audit_logs_2026_09 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-09-01 00:00:00+00') TO ('2026-10-01 00:00:00+00');

CREATE TABLE audit_logs_2026_10 PARTITION OF audit_logs
    FOR VALUES FROM ('2026-10-01 00:00:00+00') TO ('2026-11-01 00:00:00+00');

CREATE TABLE audit_logs_default PARTITION OF audit_logs DEFAULT;

-- 4. Restore backup data if any existed
INSERT INTO audit_logs (id, user_id, action_type, entity_name, details, created_at)
SELECT id, user_id, action_type, entity_name, details, created_at FROM audit_logs_backup;
DROP TABLE IF EXISTS audit_logs_backup;

-- 5. Create performance indexes for partition pruning & query filtering
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity_name ON audit_logs(entity_name);
CREATE INDEX idx_audit_logs_action_type ON audit_logs(action_type);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);
