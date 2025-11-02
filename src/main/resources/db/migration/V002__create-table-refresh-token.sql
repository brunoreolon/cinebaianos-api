CREATE TABLE public.refresh_tokens (
    id          SERIAL PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    discord_id  varchar(255) NOT NULL,
    expiry_date TIMESTAMP    NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_refresh_token_user FOREIGN KEY (discord_id) REFERENCES public.users (discord_id)
);