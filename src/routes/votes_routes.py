from flask import Blueprint, request, jsonify, current_app

from exception.column_not_found_error import ColumnNotFoundError
from exception.invalid_vote_error import InvalidVoteError
from exception.movie_not_found_error import MovieNotFoundError
from exception.spread_sheet_error import SpreadsheetError
from exception.user_not_found_error import UserNotFoundError
from exception.user_voter_not_found_error import UserVoterNotFoundError
from exception.vote_registration_error import VoteRegistrationError
from models.vote_type import VoteType
from services.votes_service import register_vote
from util.exception_util import ERROR_CODES, error_response

votes_bp = Blueprint("votes", __name__)

@votes_bp.route("/votes", methods=["POST"])
def register_vote_route():
    conn_provider = current_app.config["CONN_PROVIDER"]
    vote_data = request.get_json()

    required_fields = ["voter_id", "movie_id", "vote"]
    missing_fields = [f for f in required_fields if not vote_data.get(f)]

    if missing_fields:
        campos = ", ".join(missing_fields)
        return error_response(
            f"Campos obrigatórios ausentes: {campos}",
            "missing_required_fields_error",
            400
        )

    voter_id = vote_data["voter_id"]
    movie_id = vote_data["movie_id"]

    vote_raw = vote_data["vote"]

    try:
        vote_enum = VoteType.from_value(vote_raw)
    except InvalidVoteError:
        return error_response(f"Voto {vote_raw} é inválido", "invalid_vote_error", 400)

    try:
        register_vote(conn_provider, voter_id, movie_id, vote_enum)
        return jsonify({
            "ok": True,
            "voto": {
                "voter_id": voter_id,
                "movie_id": movie_id,
                "vote": vote_enum.label()
            }
        }), 201
    except (UserNotFoundError, UserVoterNotFoundError, MovieNotFoundError, ColumnNotFoundError) as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)
    except SpreadsheetError as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)
    except VoteRegistrationError as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)
