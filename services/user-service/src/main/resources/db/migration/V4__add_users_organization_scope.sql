ALTER TABLE users
    ADD COLUMN organization_id UUID;

CREATE INDEX idx_user_organization_id ON users(organization_id);
