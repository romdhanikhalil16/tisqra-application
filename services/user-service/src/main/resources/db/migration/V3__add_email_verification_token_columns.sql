-- Add columns for email verification flow
ALTER TABLE users
    ADD COLUMN verification_token VARCHAR(255),
    ADD COLUMN verification_token_expires_at TIMESTAMP;

-- Ensure tokens are unique when present
CREATE UNIQUE INDEX ux_users_verification_token ON users(verification_token);
