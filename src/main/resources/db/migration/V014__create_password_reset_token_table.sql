CREATE TABLE password_reset_token(
    token      VARCHAR(36) PRIMARY KEY,
    discord_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    used       BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_user
        FOREIGN KEY (discord_id)
            REFERENCES users (discord_id)
            ON DELETE CASCADE
);