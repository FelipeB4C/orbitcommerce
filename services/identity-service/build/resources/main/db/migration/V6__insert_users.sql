INSERT INTO users (full_name, email, password_hash, status)
VALUES
    ('Ana Silva', 'ana@email.com', '$2a$12$e8M9P.NWqc2sketNpY5OEO.O4GQXdnsgcyTT/rj7FuFgHr.Ghhevu', 'ACTIVE'),
    ('Carlos Souza', 'carlos@email.com', '$2a$12$e8M9P.NWqc2sketNpY5OEO.O4GQXdnsgcyTT/rj7FuFgHr.Ghhevu', 'ACTIVE'),
    ('Mariana Costa', 'mariana@email.com', '$2a$12$e8M9P.NWqc2sketNpY5OEO.O4GQXdnsgcyTT/rj7FuFgHr.Ghhevu', 'ACTIVE');


INSERT INTO user_roles (user_id, role_id)
VALUES
    ((SELECT id FROM users WHERE email = 'ana@email.com'), '54a285f4-ebb7-4e8f-ba9a-91d50b448c15'),
    ((SELECT id FROM users WHERE email = 'carlos@email.com'), '8334d546-45ab-4680-9655-c625465900bb'),
    ((SELECT id FROM users WHERE email = 'mariana@email.com'), '35382f8d-471e-4887-8ced-b0e4b277cfd4');