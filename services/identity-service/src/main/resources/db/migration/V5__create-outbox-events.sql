CREATE    TABLE outbox_events (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid (),
                                  aggregate_id UUID NOT NULL,
                                  event_type VARCHAR(100) NOT NULL,
                                  payload JSONB NOT NULL,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                  published BOOLEAN NOT NULL DEFAULT FALSE
);