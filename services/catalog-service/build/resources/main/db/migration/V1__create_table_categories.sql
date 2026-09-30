CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            name VARCHAR(120) NOT NULL,
                            slug VARCHAR(140) NOT NULL UNIQUE,
                            parent_category_id UUID REFERENCES categories(id),
                            created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);