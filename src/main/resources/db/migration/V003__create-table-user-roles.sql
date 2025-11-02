CREATE TABLE user_roles (
    discord_id varchar(255) NOT NULL,
    roles      varchar(255) NOT NULL,

    CONSTRAINT fk_user_roles_user FOREIGN KEY (discord_id) REFERENCES users (discord_id)
);
