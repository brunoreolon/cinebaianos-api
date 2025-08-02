from flask import Blueprint, current_app, jsonify

from exception.user_not_found_error import UserNotFoundError
from services.rankings_service import get_da_hora_ranking, get_lixo_ranking, get_ranking, get_da_hora_total, \
    get_lixo_total
from util.exception_util import ERROR_CODES, error_response
from auth.auth import require_auth


rankings_bp = Blueprint("rankings", __name__)


@rankings_bp.route("/ranking", methods=["GET"])
@require_auth
def get_total_votes_per_user():
    conn_provider = current_app.config["CONN_PROVIDER"]

    ranking = get_ranking(conn_provider)
    return jsonify(ranking), 200


@rankings_bp.route("/ranking/da-hora", methods=["GET"])
@require_auth
def ranking_da_hora():
    conn_provider = current_app.config["CONN_PROVIDER"]

    result = get_da_hora_ranking(conn_provider)
    return jsonify(result), 200

@rankings_bp.route("/ranking/da-hora/<string:discord_id>", methods=["GET"])
@require_auth
def da_hora_total(discord_id):
    conn_provider = current_app.config["CONN_PROVIDER"]

    try:
        user, total = get_da_hora_total(conn_provider, discord_id)

        return jsonify({
            "user": user.to_dict(),
            "total_da_hora": total
        }), 200
    except UserNotFoundError as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)

@rankings_bp.route("/ranking/lixo", methods=["GET"])
@require_auth
def ranking_lixo():
    conn_provider = current_app.config["CONN_PROVIDER"]

    result = get_lixo_ranking(conn_provider)
    return jsonify(result), 200

@rankings_bp.route("/ranking/lixos/<string:discord_id>", methods=["GET"])
@require_auth
def lixos_total(discord_id):
    conn_provider = current_app.config["CONN_PROVIDER"]

    try:
        user, total = get_lixo_total(conn_provider, discord_id)

        return jsonify({
            "user": user.to_dict(),
            "total_lixo": total
        }), 200
    except UserNotFoundError as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)