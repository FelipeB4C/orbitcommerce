CREATE TABLE price_history (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               product_variant_id UUID NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
                               price_cents BIGINT NOT NULL,
                               effective_from TIMESTAMPTZ NOT NULL,
                               effective_to TIMESTAMPTZ
);