-- ########################### USERS #########################

ALTER TABLE users
    ADD COLUMN id bigint NULL;

CREATE SEQUENCE users_id_seq;

ALTER SEQUENCE users_id_seq
    owned BY users.id;

WITH ordered_users AS (SELECT discord_id, ROW_NUMBER() OVER (ORDER BY created ASC) AS new_id
                       FROM users)
UPDATE users u
SET id = ou.new_id FROM ordered_users ou
WHERE u.discord_id = ou.discord_id;

SELECT setval('users_id_seq', (SELECT max(id) FROM users));

ALTER TABLE users
    ALTER COLUMN id SET DEFAULT nextval('users_id_seq');

ALTER TABLE users
    ALTER COLUMN id SET NOT NULL;

-- ###########################################################


-- ########################### MOVIE #########################

ALTER TABLE movie
    ADD COLUMN chooser_id_new bigint NULL;

UPDATE movie
SET chooser_id_new = users.id FROM users
WHERE movie.chooser_id = users.discord_id;

ALTER TABLE movie
    ALTER COLUMN chooser_id_new SET NOT NULL;

ALTER TABLE movie
    DROP CONSTRAINT fk_movie_chooser;

ALTER TABLE movie
    DROP COLUMN chooser_id;

ALTER TABLE movie
    rename COLUMN chooser_id_new TO chooser_id;

-- ###########################################################


-- ######################### USER_ROLES #######################

ALTER TABLE user_roles
    ADD COLUMN user_id bigint NULL;

UPDATE user_roles
SET user_id = users.id FROM users
WHERE user_roles.discord_id = users.discord_id;

ALTER TABLE user_roles
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE user_roles
    DROP CONSTRAINT fk_user_roles_user;

ALTER TABLE user_roles
    DROP COLUMN discord_id;

-- ############################################################



-- ######################### REFRESH_TOKENS ###################

ALTER TABLE refresh_tokens
    ADD COLUMN user_id bigint NULL;

UPDATE refresh_tokens
SET user_id = users.id FROM users
WHERE refresh_tokens.discord_id = users.discord_id;

ALTER TABLE refresh_tokens
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE refresh_tokens
    DROP CONSTRAINT fk_refresh_tokens_user;

ALTER TABLE refresh_tokens
    DROP COLUMN discord_id;

-- ############################################################



-- #################### PASSWORD_RESET_TOKEN ##################

ALTER TABLE password_reset_token
    ADD COLUMN user_id bigint NULL;

UPDATE password_reset_token
SET user_id = users.id FROM users
WHERE password_reset_token.discord_id = users.discord_id;

ALTER TABLE password_reset_token
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE password_reset_token
    DROP CONSTRAINT fk_user;

ALTER TABLE password_reset_token
    DROP COLUMN discord_id;

-- ############################################################



-- ############################ VOTE ##########################

ALTER TABLE vote
    ADD COLUMN voter_id_new bigint NULL;

UPDATE vote
SET voter_id_new = users.id FROM users
WHERE vote.voter_id = users.discord_id;

ALTER TABLE vote
    ALTER COLUMN voter_id_new SET NOT NULL;

ALTER TABLE vote
    DROP CONSTRAINT fk_vote_voter;

ALTER TABLE vote
    DROP COLUMN voter_id;

ALTER TABLE vote
    rename COLUMN voter_id_new TO voter_id;

-- ############################################################


-- ##################### UPDATE PRIMARY KEY ###################

ALTER TABLE users
    DROP CONSTRAINT users_pkey;

ALTER TABLE users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

ALTER TABLE users
    ALTER COLUMN discord_id DROP NOT NULL;

-- ############################################################


-- ################# RESTORE UNIQUE CONSTRAINT ################

ALTER TABLE users
    ADD CONSTRAINT users_discord_id_key UNIQUE (discord_id);

-- ############################################################


-- ################### RECREATE FOREIGN KEYS ##################

ALTER TABLE movie
    ADD CONSTRAINT fk_movie_chooser
        FOREIGN KEY (chooser_id) REFERENCES users (id) ON DELETE RESTRICT;

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE password_reset_token
    ADD CONSTRAINT fk_password_reset_token_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE vote
    ADD CONSTRAINT fk_vote_voter
        FOREIGN KEY (voter_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE vote
    ADD CONSTRAINT vote_pkey
        PRIMARY KEY (movie_id, voter_id);

-- ############################################################