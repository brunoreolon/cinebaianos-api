from flask import jsonify

from exception.column_not_found_error import ColumnNotFoundError
from exception.movie_details_fetch_error import MovieDetailsFetchError
from exception.movie_not_found_error import MovieNotFoundError
from exception.spread_sheet_error import SpreadsheetError
from exception.user_already_exists_error import UserAlreadyExistsError
from exception.user_not_found_error import UserNotFoundError
from exception.user_voter_not_found_error import UserVoterNotFoundError
from exception.vote_registration_error import VoteRegistrationError

ERROR_CODES = {
    UserVoterNotFoundError: ("user_voter_not_found_error", 404),
    UserNotFoundError: ("user_not_found_error", 404),
    UserAlreadyExistsError: ("user_already_exists_error", 409),
    MovieNotFoundError: ("movie_not_found_error", 404),
    MovieDetailsFetchError: ("movie_details_fetch_error", 404),
    ColumnNotFoundError: ("column_not_found_error", 400),
    SpreadsheetError: ("spread_sheet_error", 400),
    VoteRegistrationError: ("vote_registration_error", 500)
}

def error_response(message: str, code: str, status_code: int):
    return jsonify({
        "error": message,
        "code": code
    }), status_code