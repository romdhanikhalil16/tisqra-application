-- Create sales_analytics table
CREATE TABLE sales_analytics (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    event_id UUID,
    date DATE NOT NULL,
    hour INTEGER,
    tickets_sold INTEGER NOT NULL DEFAULT 0,
    revenue DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    net_revenue DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    commission_percentage DECIMAL(5, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_sales_org_id ON sales_analytics(organization_id);
CREATE INDEX idx_sales_event_id ON sales_analytics(event_id);
CREATE INDEX idx_sales_date ON sales_analytics(date);
CREATE INDEX idx_sales_date_hour ON sales_analytics(date, hour);

COMMENT ON TABLE sales_analytics IS 'Aggregated sales data for analytics and reporting';
