-- Create orders table
CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(50) NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    event_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    subtotal DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    promo_code VARCHAR(50),
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    confirmed_at TIMESTAMP
);

CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_order_user_id ON orders(user_id);
CREATE INDEX idx_order_event_id ON orders(event_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_expires_at ON orders(expires_at);

COMMENT ON TABLE orders IS 'Ticket purchase orders';
