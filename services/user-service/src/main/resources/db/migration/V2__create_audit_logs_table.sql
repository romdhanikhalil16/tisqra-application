-- Create audit_logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    action VARCHAR(100) NOT NULL,
    description TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    metadata TEXT,
    timestamp TIMESTAMP NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);

-- Add comments
COMMENT ON TABLE audit_logs IS 'Audit trail for user actions';
COMMENT ON COLUMN audit_logs.action IS 'Type of action performed';
COMMENT ON COLUMN audit_logs.metadata IS 'Additional context as JSON';
