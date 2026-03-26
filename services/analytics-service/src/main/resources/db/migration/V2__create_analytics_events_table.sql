-- Create analytics_events table
CREATE TABLE analytics_events (
    id UUID PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    organization_id UUID,
    data JSONB,
    occurred_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_analytics_event_type ON analytics_events(event_type);
CREATE INDEX idx_analytics_aggregate_id ON analytics_events(aggregate_id);
CREATE INDEX idx_analytics_org_id ON analytics_events(organization_id);
CREATE INDEX idx_analytics_occurred_at ON analytics_events(occurred_at);

COMMENT ON TABLE analytics_events IS 'Event tracking for analytics and user behavior analysis';
