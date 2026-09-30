CREATE TABLE variant_attributes (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    product_variant_id UUID NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
                                    attribute_name VARCHAR(60) NOT NULL,   -- ex: "size", "color"
                                    attribute_value VARCHAR(60) NOT NULL,  -- ex: "42", "azul"
                                    UNIQUE (product_variant_id, attribute_name)
);