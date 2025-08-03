from flask import Blueprint, jsonify, current_app

from exception.movie_details_fetch_error import MovieDetailsFetchError
from exception.movie_not_found_error import MovieNotFoundError
from exception.spread_sheet_error import SpreadsheetError
from services.sync_service import sync_movies_and_votes
from util.exception_util import ERROR_CODES, error_response
from auth.auth import require_auth

sync_bp = Blueprint("sync", __name__)

@sync_bp.route("/sync", methods=["POST"])
@require_auth
def sync_route():
    conn_provider = current_app.config["CONN_PROVIDER"]

    try:
        total_movies, total_votes, elapsed = sync_movies_and_votes(conn_provider)

        return jsonify({
            "message": "Sincronização concluída com sucesso",
            "total_movie": total_movies,
            "total_votes": total_votes,
            "execution_time_seconds": round(elapsed, 2)
        }), 200
    except (MovieNotFoundError, MovieDetailsFetchError, SpreadsheetError) as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)
