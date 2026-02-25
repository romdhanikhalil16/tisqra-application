-- Create ticket_categories table
CREATE TABLE ticket_categories (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    quantity INTEGER NOT NULL,
    sold_count INTEGER NOT NULL DEFAULT 0,
    reserved_count INTEGER NOT NULL DEFAULT 0,
    sale_start_date TIMESTAMP,
    sale_end_date TIMESTAMP,
    color VARCHAR(7),
    features TEXT[],
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

CREATE INDEX idx_ticket_cat_event_id ON ticket_categories(event_id);

COMMENT ON TABLE ticket_categories IS 'Ticket pricing tiers for events';
