-- Create organizations table
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    owner_id UUID NOT NULL,
    website VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    logo_url VARCHAR(500),
    is_active BOOLEAN NOT NULL DEFAULT true,
    is_verified BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_org_slug ON organizations(slug);
CREATE INDEX idx_org_owner_id ON organizations(owner_id);
CREATE INDEX idx_org_is_active ON organizations(is_active);

COMMENT ON TABLE organizations IS 'Tenant organizations';
