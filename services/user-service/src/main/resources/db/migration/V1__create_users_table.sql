-- Create users table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    keycloak_id VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT true,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    profile_image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_user_role ON users(role);
CREATE INDEX idx_user_is_active ON users(is_active);

-- Add comments
COMMENT ON TABLE users IS 'User accounts and profiles';
COMMENT ON COLUMN users.keycloak_id IS 'Reference to Keycloak user ID';
COMMENT ON COLUMN users.role IS 'User role: SUPER_ADMIN, ADMIN_ORG, SCANNER, GUEST';
