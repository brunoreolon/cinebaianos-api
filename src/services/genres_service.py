from di.repository_factory import create_genres_repository
from models.user import User
from services.users_service import get_user


def get_most_watched_genres(conn_provider):
    genre_repo = create_genres_repository(conn_provider)
    return genre_repo.count_most_watched_genres()

def get_most_voted_good_genres(conn_provider):
    genre_repo = create_genres_repository(conn_provider)
    return genre_repo.count_genres_da_hora()

def get_most_voted_bad_genres(conn_provider):
    genre_repo = create_genres_repository(conn_provider)
    return genre_repo.count_genres_lixo()

def count_genres_by_user(conn_provider, discord_id) -> tuple[User, int]:
    user = get_user(conn_provider, discord_id)
    genre_repo = create_genres_repository(conn_provider)
    genres = genre_repo.count_genres_by_user(user.discord_id)

    return user, genres