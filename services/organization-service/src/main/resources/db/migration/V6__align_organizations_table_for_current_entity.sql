-- Align existing organizations table with the current Organization entity fields.
-- Keeps legacy columns (slug, description, is_active, etc.) intact.

ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS address VARCHAR(255),
    ADD COLUMN IF NOT EXISTS city VARCHAR(100),
    ADD COLUMN IF NOT EXISTS country VARCHAR(100),
    ADD COLUMN IF NOT EXISTS domain VARCHAR(150),
    ADD COLUMN IF NOT EXISTS subscription_plan VARCHAR(50) NOT NULL DEFAULT 'BASIC',
    ADD COLUMN IF NOT EXISTS max_events INTEGER NOT NULL DEFAULT 5,
    ADD COLUMN IF NOT EXISTS event_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP,
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

