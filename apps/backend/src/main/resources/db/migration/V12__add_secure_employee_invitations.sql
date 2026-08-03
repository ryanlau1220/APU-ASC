ALTER TABLE users
    ADD COLUMN invitation_token_hash VARCHAR(64),
    ADD COLUMN invitation_expires_at TIMESTAMPTZ,
    ADD COLUMN invitation_accepted_at TIMESTAMPTZ;

CREATE UNIQUE INDEX uq_users_invitation_token_hash
    ON users (invitation_token_hash)
    WHERE invitation_token_hash IS NOT NULL;

CREATE INDEX idx_users_pending_invitation_expiry
    ON users (invitation_expires_at)
    WHERE invitation_token_hash IS NOT NULL;
