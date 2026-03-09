-- Create brandings table
CREATE TABLE brandings (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL UNIQUE,
    primary_color VARCHAR(7) DEFAULT '#007BFF',
    secondary_color VARCHAR(7) DEFAULT '#6C757D',
    accent_color VARCHAR(7) DEFAULT '#28A745',
    logo_url VARCHAR(500),
    banner_url VARCHAR(500),
    favicon_url VARCHAR(500),
    font_family VARCHAR(100) DEFAULT 'Arial, sans-serif',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_branding_org_id ON brandings(organization_id);

COMMENT ON TABLE brandings IS 'Organization branding and theme settings';
