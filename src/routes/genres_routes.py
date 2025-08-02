from flask import Blueprint, current_app, jsonify, request

from exception.user_not_found_error import UserNotFoundError
from services.genres_service import get_most_watched_genres, get_most_voted_good_genres, get_most_voted_bad_genres, \
    count_genres_by_user
from util.exception_util import error_response, ERROR_CODES

genres_bp = Blueprint("genres", __name__)

@genres_bp.route("/genres/most-watched", methods=["GET"])
def most_watched_genres():
    conn_provider = current_app.config["CONN_PROVIDER"]
    genres = get_most_watched_genres(conn_provider)
    return jsonify({"genres": genres}), 200

@genres_bp.route("/genres/most-voted-good", methods=["GET"])
def most_voted_good_genres():
    conn = current_app.config["CONN_PROVIDER"]
    genres = get_most_voted_good_genres(conn)
    return jsonify({"genres": genres}), 200

@genres_bp.route("/genres/most-voted-bad", methods=["GET"])
def most_voted_bad_genres():
    conn = current_app.config["CONN_PROVIDER"]
    genres = get_most_voted_bad_genres(conn)
    return jsonify({"genres": genres}), 200

@genres_bp.route("/genres/mine", methods=["GET"])
def get_my_genres():
    conn = current_app.config["CONN_PROVIDER"]

    discord_id = request.args.get("discord_id")
    if not discord_id:
        return error_response("Campo 'discord_id' é obrigatório", "missing_discord_id", 400)

    try:
        genres = count_genres_by_user(conn, discord_id)
        return jsonify(genres), 200
    except UserNotFoundError as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)

@genres_bp.route("/genres/user/<string:discord_id>", methods=["GET"])
def get_user_genres(discord_id):
    conn = current_app.config["CONN_PROVIDER"]

    try:
        genres = count_genres_by_user(conn, discord_id)
        return jsonify(genres), 200
    except UserNotFoundError as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)
