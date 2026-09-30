CREATE TABLE product_variants (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                  price_cents BIGINT NOT NULL,
                                  currency CHAR(3) NOT NULL DEFAULT 'CAD',
                                  stock_keeping_unit VARCHAR(40) NOT NULL UNIQUE
);