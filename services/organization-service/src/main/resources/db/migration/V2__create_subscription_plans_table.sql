-- Create subscription_plans table
CREATE TABLE subscription_plans (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    price_monthly DECIMAL(10, 2) NOT NULL,
    price_yearly DECIMAL(10, 2) NOT NULL,
    max_events_per_month INTEGER NOT NULL,
    max_tickets_per_event INTEGER NOT NULL,
    commission_percentage DECIMAL(5, 2) NOT NULL,
    features JSONB,
    is_active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER NOT NULL
);

CREATE INDEX idx_plan_code ON subscription_plans(code);
CREATE INDEX idx_plan_is_active ON subscription_plans(is_active);

COMMENT ON TABLE subscription_plans IS 'Subscription pricing plans';
