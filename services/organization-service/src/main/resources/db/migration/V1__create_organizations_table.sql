CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(200) NOT NULL UNIQUE,
    phone VARCHAR(30),
    address VARCHAR(255),
    city VARCHAR(100),
    country VARCHAR(100),
    domain VARCHAR(150),
    owner_id UUID,

    subscription_plan VARCHAR(50) NOT NULL DEFAULT 'BASIC',
    max_events INTEGER NOT NULL DEFAULT 5,
    event_count INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    version BIGINT
);

CREATE INDEX idx_organizations_owner_id ON organizations(owner_id);

