from di.repository_factory import create_movies_repository, create_users_repository, create_votes_repository
from exception.movie_details_not_found_error import MovieDetailsNotFoundError
from exception.user_not_found_error import UserNotFoundError
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
        raise MovieDetailsNotFoundError(f"Details for movie '{title}' ({year}) not found")

    movie = movie_repo.add_movie(
        title=movie_details.title,
        responsible_id=user.discord_id,
        spreadsheet_row=spreadsheet_row,
        genre=movie_details.genre if movie_details.genre else "Indefinido",
        year=movie_details.year,
        tmdb_id=movie_details.id
    )

    vote_repo.register_vote(movie.id, movie.responsible_id, movie.responsible_id, vote)


    return movie