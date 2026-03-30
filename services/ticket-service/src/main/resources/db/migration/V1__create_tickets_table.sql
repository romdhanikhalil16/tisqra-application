CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    ticket_number VARCHAR(100) NOT NULL UNIQUE,
    order_id UUID NOT NULL,
    event_id UUID NOT NULL,
    ticket_category_id UUID NOT NULL,
    qr_code VARCHAR(300) NOT NULL UNIQUE,
    qr_code_image BYTEA,

    owner_email VARCHAR(200),
    owner_name VARCHAR(200),
    owner_user_id UUID,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_transferable BOOLEAN NOT NULL DEFAULT TRUE,

    validated_at TIMESTAMP,
    validated_by UUID,
    scanner_device_id VARCHAR(100),

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    version BIGINT
);

CREATE INDEX idx_tickets_order_id ON tickets(order_id);
CREATE INDEX idx_tickets_event_id ON tickets(event_id);
CREATE INDEX idx_tickets_owner_user_id ON tickets(owner_user_id);
CREATE INDEX idx_tickets_status ON tickets(status);

