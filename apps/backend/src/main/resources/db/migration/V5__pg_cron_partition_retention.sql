-- Migration: V5__pg_cron_partition_retention.sql
-- Description: Automated monthly partition creation and retention cleanup via pg_cron

-- 1. Stored function for automated audit log partition management
CREATE OR REPLACE FUNCTION maintain_audit_log_partitions(retention_months INT DEFAULT 12)
RETURNS void AS $$
DECLARE
    next_month DATE;
    old_month DATE;
    partition_name TEXT;
    start_date TEXT;
    end_date TEXT;
BEGIN
    -- Pre-create partition for next month
    next_month := DATE_TRUNC('month', CURRENT_DATE + INTERVAL '1 month');
    partition_name := 'audit_logs_' || TO_CHAR(next_month, 'YYYY_MM');
    start_date := TO_CHAR(next_month, 'YYYY-MM-DD') || ' 00:00:00+00';
    end_date := TO_CHAR(next_month + INTERVAL '1 month', 'YYYY-MM-DD') || ' 00:00:00+00';

    EXECUTE FORMAT(
        'CREATE TABLE IF NOT EXISTS %I PARTITION OF audit_logs FOR VALUES FROM (%L) TO (%L);',
        partition_name, start_date, end_date
    );

    -- Drop partition older than specified retention period (default 12 months)
    old_month := DATE_TRUNC('month', CURRENT_DATE - (retention_months || ' months')::INTERVAL);
    partition_name := 'audit_logs_' || TO_CHAR(old_month, 'YYYY_MM');

    EXECUTE FORMAT('DROP TABLE IF EXISTS %I CASCADE;', partition_name);
END;
$$ LANGUAGE plpgsql;

-- 2. Safely initialize pg_cron schedule if available
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'pg_cron') OR
       EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'pg_cron') THEN
        CREATE EXTENSION IF NOT EXISTS pg_cron;
        PERFORM cron.schedule('audit_logs_retention_job', '0 0 1 * *', 'SELECT maintain_audit_log_partitions(12)');
    END IF;
EXCEPTION WHEN OTHERS THEN
    RAISE NOTICE 'pg_cron extension not active in current PostgreSQL environment: %', SQLERRM;
END $$;
