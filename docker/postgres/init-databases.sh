#!/bin/bash
set -e

# Script to create multiple databases in PostgreSQL
# Usage: POSTGRES_MULTIPLE_DATABASES="db1,db2,db3"

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create databases for microservices
    CREATE DATABASE user_db;
    CREATE DATABASE organization_db;
    CREATE DATABASE event_db;
    CREATE DATABASE order_db;
    CREATE DATABASE ticket_db;
    CREATE DATABASE payment_db;
    CREATE DATABASE notification_db;
    CREATE DATABASE analytics_db;
    CREATE DATABASE keycloak_db;

    -- Grant privileges
    GRANT ALL PRIVILEGES ON DATABASE user_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE organization_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE event_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE order_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE ticket_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE payment_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE notification_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE analytics_db TO $POSTGRES_USER;
    GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO $POSTGRES_USER;
EOSQL

echo "✓ All databases created successfully"
