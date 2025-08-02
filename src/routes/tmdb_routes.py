from flask import Blueprint, request, jsonify

from exception.movie_details_fetch_error import MovieDetailsFetchError
from exception.movie_not_found_error import MovieNotFoundError
from services.tmdb_service import fetch_movie_details
from util.exception_util import ERROR_CODES, error_response

tmdb_bp = Blueprint("tmdb", __name__)

@tmdb_bp.route("/movie-details", methods=["POST"])
def movie_details_route():
    data = request.get_json()
    title = data.get("title")
    year = data.get("year")

    if not title:
        return error_response("Campo 'title' é obrigatório", "missing_title_error", 400)

    try:
        filme = fetch_movie_details(title, year)
        return jsonify(filme.to_dict()), 200
    except (MovieDetailsFetchError, MovieNotFoundError) as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)
