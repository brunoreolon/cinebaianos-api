from flask import Blueprint, request, jsonify, current_app

from exception.column_not_found_error import ColumnNotFoundError
from exception.invalid_vote_error import InvalidVoteError
from exception.movie_not_found_error import MovieNotFoundError
from exception.spread_sheet_error import SpreadsheetError
from exception.user_not_found_error import UserNotFoundError
from exception.user_voter_not_found_error import UserVoterNotFoundError
from exception.vote_registration_error import VoteRegistrationError
from models.vote_type import VoteType
from services.movies_service import get_movie_by_id
from services.users_service import get_user
from services.votes_service import register_vote_spreadsheet, register_vote_db
from util.exception_util import ERROR_CODES, error_response
from auth.auth import require_auth

votes_bp = Blueprint("votes", __name__)

@votes_bp.route("/votes", methods=["POST"])
@require_auth
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
        movie = get_movie_by_id(conn_provider, movie_id)
        responsible = get_user(conn_provider, movie.responsible_id)
    except MovieNotFoundError as e:
        return error_response(f"Filme com ID '{movie_id}' não encontrado.", "movie_not_found_error", 404)

    try:
        voter = get_user(conn_provider, voter_id)
    except UserNotFoundError:
        return error_response(f"Usuário votante com ID '{voter_id}' não encontrado.", "user_voter_not_found_error", 404)

    try:
        vote_enum = VoteType.from_value(vote_raw)
    except InvalidVoteError:
        return error_response(f"Voto {vote_raw} é inválido", "invalid_vote_error", 400)

    try:
        register_vote_spreadsheet(conn_provider, voter, movie, vote_enum)
    except (UserNotFoundError, UserVoterNotFoundError, MovieNotFoundError, ColumnNotFoundError,
            SpreadsheetError, VoteRegistrationError) as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)

    try:
        register_vote_db(conn_provider, movie_id, responsible.discord_id, voter_id, vote_enum.value)
    except (UserNotFoundError, UserVoterNotFoundError, MovieNotFoundError, ColumnNotFoundError, SpreadsheetError,
            VoteRegistrationError) as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)

    movie_dict = movie.to_dict()
    movie_dict["responsible"] = responsible.to_dict()

    return jsonify({
        "vote": {
            "vote": vote_enum.label(),
            "voter_id": voter.discord_id,
            "movie_id": movie_id
        },
        "voter": voter.to_dict(),
        "movie": movie_dict
    }), 201
