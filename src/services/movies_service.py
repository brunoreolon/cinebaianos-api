from di.repository_factory import create_movies_repository, create_users_repository, create_votes_repository
from exception.invalid_vote_error import InvalidVoteError
from exception.movie_details_fetch_error import MovieDetailsFetchError
from exception.movie_not_found_error import MovieNotFoundError
from exception.user_not_found_error import UserNotFoundError
from models.vote_type import VoteType
from services.sheet_service import add_movie_to_spreadsheet
from services.tmdb_service import fetch_movie_details
from services.users_service import get_user


def add_movie(conn_provider, title, year, responsible_id, spreadsheet_row, vote):
    movie_repo = create_movies_repository(conn_provider)

    user = get_user(conn_provider, responsible_id)
    if not user:
        raise UserNotFoundError(f"usuário {responsible_id} não encontrado")

    movie_details = fetch_movie_details(title, year)
    if not movie_details:
        raise MovieDetailsFetchError(f"Detalhes para o filme '{title}' ({year}) não encontrado")

    if vote is not None:
        try:
            vote_int = int(vote)
            vote_enum = VoteType.from_value(vote_int)
            vote_name = vote_enum.label()
            vote_value = vote_enum.value
        except InvalidVoteError as e:
            raise e
    else:
        vote_enum = None
        vote_name = None
        vote_value = None

    add_movie_to_spreadsheet(
        movie_details.title,
        user.tab,
        user.column,
        vote=vote_name
    )

    movie = movie_repo.add_movie(
        title=movie_details.title,
        responsible_id=user.discord_id,
        spreadsheet_row=spreadsheet_row,
        genre=movie_details.genre if movie_details.genre else "Indefinido",
        year=movie_details.year,
        tmdb_id=movie_details.id
    )

    if vote_enum is not None:
        from services.votes_service import register_vote_db
        register_vote_db(conn_provider, movie.id, movie.responsible_id, movie.responsible_id, vote_value)

    return {
        "movie": movie,
        "responsible": user,
        "vote": vote_enum  # pode ser None
    }

def get_all_movies_grouped_by_user(conn_provider):
    movie_repo = create_movies_repository(conn_provider)

    movies = movie_repo.find_all()
    grouped = {}

    for movie in movies:
        user_id = movie.responsible_id
        if user_id not in grouped:
            user = get_user(conn_provider, user_id)
            grouped[user_id] = {
                "user": user,
                "movies": []
            }
        grouped[user_id]["movies"].append(movie)

    return list(grouped.values())

def get_all_user_movies(conn_provider, user_id):
    movie_repo = create_movies_repository(conn_provider)

    user = get_user(conn_provider, user_id)

    if not user:
        raise UserNotFoundError(f"Usuário {user_id} não encontrado")

    movies = movie_repo.find_movies_by_user(user.discord_id)

    return movies

def get_movie_by_id(conn_provider, movie_id):
    movie_repo = create_movies_repository(conn_provider)

    movie = movie_repo.find_movie_by_id(movie_id)

    if not movie:
        raise MovieNotFoundError(f"Filme {movie_id} não encontrado")

    return movie

def get_movie_by_row_and_user(conn_provider, responsible_id, row_id):
    movie_repo = create_movies_repository(conn_provider)

    movie = movie_repo.find_movie_by_row_and_user(responsible_id, row_id)

    return movie