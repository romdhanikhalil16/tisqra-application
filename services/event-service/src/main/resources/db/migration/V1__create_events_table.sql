-- Create events table
CREATE TABLE events (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    name VARCHAR(300) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    name VARCHAR(200) NOT NULL,
    address VARCHAR(500) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100),
    zip_code VARCHAR(20),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    maps_url VARCHAR(500),
    capacity INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    banner_image_url VARCHAR(500),
    thumbnail_image_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    published_at TIMESTAMP
);

CREATE INDEX idx_event_slug ON events(slug);
CREATE INDEX idx_event_org_id ON events(organization_id);
CREATE INDEX idx_event_status ON events(status);
CREATE INDEX idx_event_start_date ON events(start_date);
CREATE INDEX idx_event_category ON events(category);

COMMENT ON TABLE events IS 'Events and their details';
