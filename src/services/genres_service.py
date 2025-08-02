from di.repository_factory import create_genres_repository


def get_most_watched_genres(conn_provider):
    genre_repo = create_genres_repository(conn_provider)

    raw_genres = genre_repo.count_most_watched_genres()
    genres = [{"genre": g, "count": c} for g, c in raw_genres]

    return genres

def get_most_voted_good_genres(conn_provider):
    genre_repo = create_genres_repository(conn_provider)

    raw_genres = genre_repo.count_genres_da_hora()
    genres = [{"genre": g, "count": c} for g, c in raw_genres]

    return genres

def get_most_voted_bad_genres(conn_provider):
    genre_repo = create_genres_repository(conn_provider)

    raw_genres = genre_repo.count_genres_da_hora()
    genres = [{"genre": g, "count": c} for g, c in raw_genres]

    return genres

def count_genres_by_user(conn_provider, discord_id):
    genre_repo = create_genres_repository(conn_provider)

    raw_genres = genre_repo.count_genres_by_user(discord_id)
    genres = [{"genre": g, "count": c} for g, c in raw_genres]

    return genres