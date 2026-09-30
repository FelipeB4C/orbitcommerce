CREATE TABLE products (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          sku VARCHAR(40) NOT NULL UNIQUE,
                          name VARCHAR(200) NOT NULL,
                          description TEXT,
                          category_id UUID NOT NULL REFERENCES categories(id),
                          brand VARCHAR(100),
                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE | INACTIVE | DISCONTINUED
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_products_category ON products(category_id);