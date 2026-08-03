-- Spring Modulith's event_publication table is created by V1. These partial indexes keep replay
-- and retention maintenance bounded as publication volume grows.
CREATE INDEX IF NOT EXISTS idx_event_publication_incomplete
    ON event_publication (publication_date)
    WHERE completion_date IS NULL;

CREATE INDEX IF NOT EXISTS idx_event_publication_completed
    ON event_publication (completion_date)
    WHERE completion_date IS NOT NULL;
