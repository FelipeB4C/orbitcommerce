CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE variant_prices (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               product_variant_id UUID NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
                               currency CHAR(3) NOT NULL,
                               price_cents BIGINT NOT NULL CHECK (price_cents >= 0),
                               effective_from TIMESTAMPTZ NOT NULL,
                               effective_to TIMESTAMPTZ,
                               CONSTRAINT ck_variant_prices_period
                                   CHECK (effective_to IS NULL OR effective_to > effective_from),
                               CONSTRAINT ex_variant_prices_no_overlap
                                   EXCLUDE USING gist (
            product_variant_id WITH =,
            currency WITH =,
            tstzrange(effective_from, effective_to, '[)') WITH &&
        )
);