-- Create order_items table
CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    ticket_category_id UUID NOT NULL,
    ticket_category_name VARCHAR(150) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_order_item_order_id ON order_items(order_id);

COMMENT ON TABLE order_items IS 'Items in an order (ticket categories)';
