-- Create promo_codes table
CREATE TABLE promo_codes (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    event_id UUID NOT NULL,
    discount_type VARCHAR(20) NOT NULL,
    discount_value DECIMAL(10, 2) NOT NULL,
    max_uses INTEGER,
    used_count INTEGER NOT NULL DEFAULT 0,
    valid_from TIMESTAMP NOT NULL,
    valid_until TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

CREATE INDEX idx_promo_code ON promo_codes(code);
CREATE INDEX idx_promo_event_id ON promo_codes(event_id);

COMMENT ON TABLE promo_codes IS 'Promotional discount codes for events';
