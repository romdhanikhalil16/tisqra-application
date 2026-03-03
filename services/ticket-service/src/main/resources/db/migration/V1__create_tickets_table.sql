-- Create tickets table
CREATE TABLE tickets (
    id UUID PRIMARY KEY,
    ticket_number VARCHAR(50) NOT NULL UNIQUE,
    order_id UUID NOT NULL,
    event_id UUID NOT NULL,
    ticket_category_id UUID NOT NULL,
    ticket_category_name VARCHAR(150) NOT NULL,
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    qr_code_image BYTEA,
    owner_email VARCHAR(255) NOT NULL,
    owner_name VARCHAR(150) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255),
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    is_transferable BOOLEAN NOT NULL DEFAULT true,
    validated_at TIMESTAMP,
    validated_by UUID,
    validated_by_name VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX idx_ticket_number ON tickets(ticket_number);
CREATE INDEX idx_ticket_qr_code ON tickets(qr_code);
CREATE INDEX idx_ticket_order_id ON tickets(order_id);
CREATE INDEX idx_ticket_event_id ON tickets(event_id);
CREATE INDEX idx_ticket_owner_email ON tickets(owner_email);
CREATE INDEX idx_ticket_status ON tickets(status);

COMMENT ON TABLE tickets IS 'Event tickets with QR codes';
