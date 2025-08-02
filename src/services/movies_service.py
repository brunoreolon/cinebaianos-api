from flask import jsonify

from di.repository_factory import create_movies_repository, create_users_repository, create_votes_repository
from exception.invalid_vote_error import InvalidVoteError
from exception.movie_details_fetch_error import MovieDetailsFetchError
from exception.movie_not_found_error import MovieNotFoundError
from exception.user_not_found_error import UserNotFoundError
from models.vote_type import VoteType
from services.sheet_service import add_movie_to_spreadsheet
from services.tmdb_service import fetch_movie_details

def add_movie(conn_provider, title, year, responsible_id, spreadsheet_row, vote):
    movie_repo = create_movies_repository(conn_provider)
    user_repo = create_users_repository(conn_provider)
    vote_repo = create_votes_repository(conn_provider)

    user = user_repo.get_user(responsible_id)

    if not user:
        raise UserNotFoundError(f"User {responsible_id} not found")

    movie_details = fetch_movie_details(title, year)

    if not movie_details:
        raise MovieDetailsFetchError(f"Details for movie '{title}' ({year}) not found")

    if vote is not None:
        try:
            vote_int = int(vote)
            vote_enum = VoteType.from_value(vote_int)
            vote_name = vote_enum.label()
            vote_value = vote_enum.value
        except InvalidVoteError as e:
            return jsonify({"error": str(e)}), 400
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
        vote_repo.register_vote(movie.id, movie.responsible_id, movie.responsible_id, vote_value)

    return movie

def get_all_movies_grouped_by_user(conn_provider):
    movie_repo = create_movies_repository(conn_provider)
    movies = movie_repo.find_all()

    grouped = {}
    for movie in movies:
        user_id = movie.responsible_id
        grouped.setdefault(user_id, []).append(movie)

    return grouped

def get_all_user_movies(conn_provider, user_id):
    movie_repo = create_movies_repository(conn_provider)
    user_repo = create_users_repository(conn_provider)

    user = user_repo.get_user(user_id)

    if not user:
        raise UserNotFoundError(f"User {user_id} not found")

    movies = movie_repo.find_movies_by_user(user.discord_id)

    return movies

def get_movie_by_id(conn_provider, movie_id):
    movie_repo = create_movies_repository(conn_provider)
    movie = movie_repo.find_movie_by_id(movie_id)

    if not movie:
        raise MovieNotFoundError(f"Movie {movie_id} not found")

    return movie