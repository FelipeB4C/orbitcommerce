CREATE TABLE product_variants (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
                                  stock_keeping_unit VARCHAR(40) NOT NULL UNIQUE
);