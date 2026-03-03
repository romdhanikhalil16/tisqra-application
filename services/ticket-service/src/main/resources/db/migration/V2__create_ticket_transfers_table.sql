-- Create ticket_transfers table
CREATE TABLE ticket_transfers (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,
    from_email VARCHAR(255) NOT NULL,
    to_email VARCHAR(255) NOT NULL,
    message TEXT,
    accepted BOOLEAN NOT NULL DEFAULT false,
    accepted_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);

CREATE INDEX idx_transfer_ticket_id ON ticket_transfers(ticket_id);
CREATE INDEX idx_transfer_to_email ON ticket_transfers(to_email);

COMMENT ON TABLE ticket_transfers IS 'Ticket ownership transfer records';
