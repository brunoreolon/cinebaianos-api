CREATE TABLE signup_verifications (
    email VARCHAR(255) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    password VARCHAR(255) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    resend_count INTEGER NOT NULL DEFAULT 0,
    last_sent_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_signup_verifications_expires_at ON signup_verifications (expires_at);