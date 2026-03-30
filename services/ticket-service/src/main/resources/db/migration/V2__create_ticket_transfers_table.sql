CREATE TABLE ticket_transfers (
    id UUID PRIMARY KEY,
    ticket_id UUID NOT NULL,

    from_email VARCHAR(200),
    to_email VARCHAR(200),
    message VARCHAR(500),

    accepted BOOLEAN NOT NULL DEFAULT TRUE,
    accepted_at TIMESTAMP,

    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    deleted_at TIMESTAMP,
    version BIGINT
);

CREATE INDEX idx_ticket_transfers_ticket_id ON ticket_transfers(ticket_id);

