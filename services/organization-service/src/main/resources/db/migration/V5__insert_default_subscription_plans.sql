-- Insert default subscription plans
INSERT INTO subscription_plans (id, name, code, description, price_monthly, price_yearly, max_events_per_month, max_tickets_per_event, commission_percentage, features, is_active, sort_order)
VALUES 
(
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Free',
    'FREE',
    'Perfect for getting started',
    0.00,
    0.00,
    2,
    100,
    10.00,
    '{"custom_branding": false, "analytics": false, "email_support": true, "api_access": false}'::jsonb,
    true,
    1
),
(
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22',
    'Starter',
    'STARTER',
    'Great for small events',
    29.99,
    299.99,
    10,
    500,
    8.00,
    '{"custom_branding": true, "analytics": true, "email_support": true, "api_access": false}'::jsonb,
    true,
    2
),
(
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33',
    'Professional',
    'PROFESSIONAL',
    'Best for growing businesses',
    99.99,
    999.99,
    50,
    2000,
    6.00,
    '{"custom_branding": true, "analytics": true, "email_support": true, "api_access": true, "priority_support": true}'::jsonb,
    true,
    3
),
(
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44',
    'Enterprise',
    'ENTERPRISE',
    'For large-scale operations',
    299.99,
    2999.99,
    999,
    10000,
    4.00,
    '{"custom_branding": true, "analytics": true, "email_support": true, "api_access": true, "priority_support": true, "dedicated_account_manager": true}'::jsonb,
    true,
    4
);
