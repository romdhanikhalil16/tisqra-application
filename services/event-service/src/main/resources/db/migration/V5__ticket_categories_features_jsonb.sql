-- Hibernate 6 friendly mapping: store TicketCategory.features as jsonb.
-- Converts legacy TEXT[] column to jsonb when upgrading an existing DB volume.

ALTER TABLE ticket_categories
    ALTER COLUMN features TYPE jsonb
    USING CASE
        WHEN features IS NULL THEN '[]'::jsonb
        ELSE to_jsonb(features)
    END;

