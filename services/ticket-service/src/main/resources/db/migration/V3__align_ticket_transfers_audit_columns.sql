-- Align legacy ticket_transfers schema with current BaseEntity columns + constraints.
-- This is needed when the DB volume was created with older migrations/schema.

ALTER TABLE ticket_transfers
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Current entity fields are nullable and capped at 200 chars.
ALTER TABLE ticket_transfers
    ALTER COLUMN from_email DROP NOT NULL,
    ALTER COLUMN to_email DROP NOT NULL;

ALTER TABLE ticket_transfers
    ALTER COLUMN from_email TYPE VARCHAR(200),
    ALTER COLUMN to_email TYPE VARCHAR(200);

-- Match entity default (builder default = true).
ALTER TABLE ticket_transfers
    ALTER COLUMN accepted SET DEFAULT TRUE;

