CREATE TABLE roles (
                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name            VARCHAR(50)  NOT NULL UNIQUE,
                       description     VARCHAR(255)
);

INSERT INTO roles(name, description) VALUES('CUSTOMER', 'BROWSER AND BUYER ACCESS WITH ORDER AND PROFILE MANAGEMENT');
INSERT INTO roles(name, description) VALUES('SELLER', 'MANAGES PRODUCTS, INVENTORY, STORE ORDERS, AND SHIPMENTS');
INSERT INTO roles(name, description) VALUES('ADMIN', 'COMPLETE ADMINISTRATIVE ACCESS OVER E-COMMERCE CONFIGURATIONS');