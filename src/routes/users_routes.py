from flask import Blueprint, request, jsonify, current_app

from auth.auth import require_auth
from exception.user_already_exists_error import UserAlreadyExistsError
from exception.user_not_found_error import UserNotFoundError
from services.users_service import create_user, get_user, get_all
from util.exception_util import error_response, ERROR_CODES

users_bp = Blueprint("users", __name__)

@users_bp.route("/users", methods=["POST"])
@require_auth
def register_route():
    conn_provider = current_app.config["CONN_PROVIDER"]
    user_data = request.get_json()

    required_fields = ["discord_id", "name", "tab", "column"]
    missing_fields = [f for f in required_fields if not user_data.get(f)]

    if missing_fields:
        campos = ", ".join(missing_fields)
        return error_response(
            f"Campos obrigatórios ausentes: {campos}",
            "missing_required_fields_error",
            400
        )

    try:
        user = create_user(
            conn_provider,
            user_data["discord_id"],
            user_data["name"],
            user_data["tab"],
            user_data["column"]
        )

        return jsonify(user.to_dict()), 201, {
            "Location": f"/users/{user.discord_id}"
        }
    except UserAlreadyExistsError as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)

@users_bp.route("/users/<string:discord_id>", methods=["GET"])
@require_auth
def get_user_by_discord_id_route(discord_id):
    conn_provider = current_app.config["CONN_PROVIDER"]

    try:
        user = get_user(conn_provider, discord_id)
        return jsonify(user.to_dict()), 200
    except UserNotFoundError as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)

@users_bp.route("/users", methods=["GET"])
@require_auth
def get_users_route():
    conn_provider = current_app.config["CONN_PROVIDER"]
    users = get_all(conn_provider)
    users_dict = [user.to_dict() for user in users]

    return jsonify(users_dict), 200