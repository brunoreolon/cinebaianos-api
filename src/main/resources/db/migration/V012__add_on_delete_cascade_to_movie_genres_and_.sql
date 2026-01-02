-- =========================
-- movie_genres adjustments
-- =========================

-- FK para movie_id: ao apagar um filme, apaga automaticamente as junções na tabela movie_genres
ALTER TABLE movie_genres
DROP CONSTRAINT movie_genres_movie_id_fkey;

ALTER TABLE movie_genres
    ADD CONSTRAINT fk_movie_genres_movie
        FOREIGN KEY (movie_id)
            REFERENCES movie(id)
            ON DELETE CASCADE;

-- FK para genre_id: ao apagar um gênero, apaga automaticamente as junções na tabela movie_genres
ALTER TABLE movie_genres
DROP CONSTRAINT movie_genres_genre_id_fkey;

ALTER TABLE movie_genres
    ADD CONSTRAINT fk_movie_genres_genre
        FOREIGN KEY (genre_id)
            REFERENCES genre(id)
            ON DELETE CASCADE;


-- =========================
-- vote adjustments
-- =========================

-- FK para movie_id: ao apagar um filme, apaga automaticamente todos os votos relacionados
ALTER TABLE vote
DROP CONSTRAINT fk_vote_movie_movie_id;

ALTER TABLE vote
    ADD CONSTRAINT fk_vote_movie
        FOREIGN KEY (movie_id)
            REFERENCES movie(id)
            ON DELETE CASCADE;

-- FK para voter_id: ao apagar um usuário, apaga automaticamente os votos que ele deu
ALTER TABLE vote
DROP CONSTRAINT fk_vote_users_voter_id;

ALTER TABLE vote
    ADD CONSTRAINT fk_vote_voter
        FOREIGN KEY (voter_id)
            REFERENCES users(discord_id)
            ON DELETE CASCADE;

-- FK para type_id: impede apagar tipos de voto que já possuem votos
ALTER TABLE vote
DROP CONSTRAINT fk_vote_vote_type_type_id;

ALTER TABLE vote
    ADD CONSTRAINT fk_vote_type
        FOREIGN KEY (type_id)
            REFERENCES vote_type(id)
            ON DELETE RESTRICT;


-- =========================
-- movie → chooser (User) adjustment
-- =========================

-- FK para chooser_id: impede apagar usuários que ainda possuem filmes
ALTER TABLE movie
DROP CONSTRAINT fk_movie_users_chooser_id;

ALTER TABLE movie
    ADD CONSTRAINT fk_movie_chooser
        FOREIGN KEY (chooser_id)
            REFERENCES users(discord_id)
            ON DELETE RESTRICT;


-- =========================
-- user_roles adjustments
-- =========================

-- FK para user_roles: deletar associações ao apagar usuário
ALTER TABLE user_roles
DROP CONSTRAINT fk_user_roles_user;

ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_user
        FOREIGN KEY (discord_id)
            REFERENCES users(discord_id)
            ON DELETE CASCADE;


-- =========================
-- refresh_tokens adjustments
-- =========================

-- FK para refresh_tokens: deletar tokens ao apagar usuário
ALTER TABLE refresh_tokens
DROP CONSTRAINT fk_refresh_token_user;

ALTER TABLE refresh_tokens
    ADD CONSTRAINT fk_refresh_tokens_user
        FOREIGN KEY (discord_id)
            REFERENCES users(discord_id)
            ON DELETE CASCADE;