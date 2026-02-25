-- Create event_schedules table
CREATE TABLE event_schedules (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    time TIME NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    speaker VARCHAR(150),
    location VARCHAR(100),
    sort_order INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

CREATE INDEX idx_schedule_event_id ON event_schedules(event_id);

COMMENT ON TABLE event_schedules IS 'Event agenda and schedule items';
