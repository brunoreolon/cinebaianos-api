-- ########################### GENRE ##########################

ALTER TABLE genre
    ADD CONSTRAINT genre_name_key UNIQUE (name);

ALTER TABLE genre
    RENAME TO genres;

-- ############################################################



-- ######################### USER_ROLES #######################

ALTER TABLE user_roles
ALTER COLUMN roles TYPE varchar(30);

-- ############################################################



-- ########################### MOVIE ##########################

ALTER TABLE movie
ALTER COLUMN year TYPE smallint USING year::smallint;

ALTER TABLE movie
ALTER COLUMN tmdb_id TYPE bigint USING tmdb_id::bigint;

ALTER TABLE movie
    ADD CONSTRAINT movie_tmdb_key UNIQUE (tmdb_id);

ALTER TABLE movie
    ALTER COLUMN poster_path DROP NOT NULL;

ALTER TABLE movie
    ALTER COLUMN date_added SET NOT NULL;

ALTER TABLE movie
    RENAME TO movies;

-- ############################################################



-- ######################### VOTE_TYPE ########################

ALTER TABLE vote_type
ALTER COLUMN description TYPE varchar(255);

ALTER TABLE vote_type
    ALTER COLUMN active SET DEFAULT true;

ALTER TABLE vote_type
    ADD COLUMN created_at timestamp NOT NULL DEFAULT now();

-- ############################################################



-- ############################ VOTE ##########################

ALTER TABLE vote
    RENAME COLUMN created TO created_at;

ALTER TABLE vote
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE vote
    RENAME COLUMN updated TO updated_at;

ALTER TABLE vote
    ALTER COLUMN updated_at SET NOT NULL;

-- ############################################################



-- ########################### USERS ##########################

ALTER TABLE users
    ALTER COLUMN password SET NOT NULL;

ALTER TABLE users
    RENAME COLUMN created TO created_at;

ALTER TABLE users
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE users
    RENAME COLUMN updated TO updated_at;

ALTER TABLE users
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE users
    ADD COLUMN banned_at timestamp NULL;

ALTER TABLE users
    ADD COLUMN banned_by bigint NULL;

ALTER TABLE users
    ADD CONSTRAINT fk_users_banned_by
        FOREIGN KEY (banned_by) REFERENCES users (id) ON DELETE SET NULL;

ALTER TABLE users
    ADD COLUMN ban_reason text NULL;

ALTER TABLE users
    ADD COLUMN expires_at timestamp NULL;

-- ############################################################