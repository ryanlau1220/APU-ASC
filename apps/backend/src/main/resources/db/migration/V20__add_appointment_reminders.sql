ALTER TABLE appointments
    ADD COLUMN reminder_queued_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_appointments_reminder_queue
    ON appointments (appointment_date, status)
    WHERE reminder_queued_at IS NULL;
