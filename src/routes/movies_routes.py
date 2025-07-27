from flask import Blueprint, request, jsonify, current_app

from exception.movie_details_not_found_error import MovieDetailsNotFoundError
from exception.user_not_found_error import UserNotFoundError
from services.movies_service import add_movie

movies_bp = Blueprint("movies", __name__)

@movies_bp.route("/movies", methods=["POST"])
def add_movie_route():
    conn_provider = current_app.config["CONN_PROVIDER"]

    movie_data = request.get_json()
    title = movie_data.get("title")

    if not title:
        return jsonify({"erro": "Campo 'title' é obrigatórios"}), 400

    try:
        movie = add_movie(
            conn_provider,
            title,
            movie_data.get("year"),
            movie_data.get("responsible_id"),
            movie_data.get("spreadsheet_row"),
            movie_data.get("vote")
        )
        return jsonify(movie.to_dict()), 201
    except UserNotFoundError as e:
        return jsonify({"error": str(e)}), 404
    except MovieDetailsNotFoundError as e:
        return jsonify({"error": str(e)}), 404



