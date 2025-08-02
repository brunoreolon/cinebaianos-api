from flask import Blueprint, request, jsonify, current_app

from exception.movie_details_fetch_error import MovieDetailsFetchError
from exception.movie_not_found_error import MovieNotFoundError
from exception.user_not_found_error import UserNotFoundError
from services.movies_service import add_movie, get_all_movies_grouped_by_user, get_all_user_movies
from util.exception_util import error_response, ERROR_CODES

movies_bp = Blueprint("movies", __name__)

@movies_bp.route("/movies", methods=["POST"])
def add_movie_route():
    conn_provider = current_app.config["CONN_PROVIDER"]

    movie_data = request.get_json()
    title = movie_data.get("title")

    required_fields = ["title", "responsible_id", "spreadsheet_row"]
    missing_fields = [f for f in required_fields if not movie_data.get(f)]

    if missing_fields:
        campos = ", ".join(missing_fields)
        return error_response(
            f"Campos obrigatórios ausentes: {campos}",
            "missing_required_fields",
            400
        )

    try:
        movie = add_movie(
            conn_provider,
            title,
            movie_data.get("year"),
            movie_data.get("responsible_id"),
            movie_data.get("spreadsheet_row"),
            movie_data.get("vote")
        )
        return jsonify(movie.to_dict()), 201, {
            "Location": f"/movies/{movie.id}"
        }
    except (UserNotFoundError, MovieNotFoundError, MovieDetailsFetchError)  as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)

@movies_bp.route("/movies", methods=["GET"])
def list_movies_route():
    conn_provider = current_app.config["CONN_PROVIDER"]

    discord_id = request.args.get("discord_id")

    try:
        if discord_id :
            movies = get_all_user_movies(conn_provider, discord_id)
            result = [movie.to_dict() for movie in movies]
        else:
            grouped_movies = get_all_movies_grouped_by_user(conn_provider)
            result = {
                user: [movie.to_dict() for movie in movies]
                for user, movies in grouped_movies.items()
            }
    except UserNotFoundError as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)

    return jsonify(result), 200