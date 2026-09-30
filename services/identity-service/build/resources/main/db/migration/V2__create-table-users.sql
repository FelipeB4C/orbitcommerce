CREATE TABLE users (
                       id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       full_name       VARCHAR(150) NOT NULL,
                       email           VARCHAR(180) NOT NULL UNIQUE,
                       password_hash   VARCHAR(255) NOT NULL,
                       status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    -- ACTIVE | BLOCKED | PENDING_VERIFICATION
                       created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
                       updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_email ON users(email)