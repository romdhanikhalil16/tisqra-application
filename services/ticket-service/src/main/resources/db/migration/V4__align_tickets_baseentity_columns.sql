-- Align legacy tickets schema with current Ticket entity + BaseEntity columns.
-- This is needed when the DB volume was created with an older schema.

ALTER TABLE tickets
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS scanner_device_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS owner_user_id UUID;

-- Align column sizes / nullability with current entity mappings.
ALTER TABLE tickets
    ALTER COLUMN ticket_number TYPE VARCHAR(100),
    ALTER COLUMN qr_code TYPE VARCHAR(300),
    ALTER COLUMN owner_email TYPE VARCHAR(200),
    ALTER COLUMN owner_name TYPE VARCHAR(200);

ALTER TABLE tickets
    ALTER COLUMN owner_email DROP NOT NULL,
    ALTER COLUMN owner_name DROP NOT NULL;

