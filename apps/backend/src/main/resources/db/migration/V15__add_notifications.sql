CREATE TABLE notifications (
    id VARCHAR(64) PRIMARY KEY,
    event_id UUID NOT NULL,
    recipient_user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(64) NOT NULL,
    title VARCHAR(160) NOT NULL,
    body VARCHAR(500) NOT NULL,
    link VARCHAR(512),
    read_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_notifications_recipient_event UNIQUE (recipient_user_id, event_id)
);

CREATE INDEX idx_notifications_recipient_created
    ON notifications(recipient_user_id, created_at DESC);
