import secrets

import jwt

from flask import Blueprint, request, jsonify, current_app
from datetime import datetime, timedelta

from exception.authentication_error import AuthenticationError
from services.auth_service import authenticate_user, delete, get, save, generate_and_save_refresh_token
from exception.user_not_found_error import UserNotFoundError
from services.users_service import get_user
from util.exception_util import ERROR_CODES, error_response

auth_bp = Blueprint("auth", __name__)

@auth_bp.route("/auth/login", methods=["POST"])
def login_route():
    conn_provider = current_app.config["CONN_PROVIDER"]
    auth_data = request.get_json()

    required_fields = ["email", "password"]
    missing_fields = [f for f in required_fields if not auth_data.get(f)]

    if missing_fields:
        campos = ", ".join(missing_fields)
        return error_response(
            f"Campos obrigatórios ausentes: {campos}",
            "missing_required_fields_error",
            400
        )

    try:
        access_token, discord_id = authenticate_user(
            conn_provider,
            auth_data["email"],
            auth_data["password"]
        )
        refresh_token = generate_and_save_refresh_token(conn_provider, discord_id)

        return jsonify({
            "access_token": access_token,
            "refresh_token": refresh_token,
            "token_type": "Bearer",
            "expires_in": 7200
        }), 200
    except UserNotFoundError as e:
        code, status = ERROR_CODES.get(type(e), ("unknown_error", 400))
        return error_response(str(e), code, status)
    except AuthenticationError:
        return error_response("Email ou senha inválidos", "authentication_error", 401)


@auth_bp.route("/auth/refresh", methods=["POST"])
def refresh_token_route():
    conn_provider = current_app.config["CONN_PROVIDER"]

    data = request.get_json()
    refresh_token = data.get("refresh_token")

    if not refresh_token:
        return jsonify({"error": "Refresh token ausente"}), 400

    record = get(conn_provider, refresh_token)

    if not record:
        return jsonify({"error": "Refresh token inválido"}), 401

    discord_id, expires_at = record

    if expires_at < datetime.utcnow():
        delete(conn_provider, refresh_token)
        return jsonify({"error": "Refresh token expirado"}), 401

    user = get_user(conn_provider, discord_id)

    if not user:
        return jsonify({"error": "Usuário não encontrado"}), 404

    access_payload = {
        "discord_id": user.discord_id,
        "name": user.name,
        "exp": datetime.utcnow() + timedelta(hours=2)
    }
    access_token = jwt.encode(access_payload, current_app.config["JWT_SECRET"], algorithm="HS256")

    # (Opcional) Gera novo refresh token e invalida o antigo
    delete(conn_provider, refresh_token)
    new_refresh_token = secrets.token_hex(32)
    save(conn_provider, new_refresh_token, user.discord_id, datetime.utcnow() + timedelta(days=7))

    return jsonify({
        "access_token": access_token,
        "refresh_token": new_refresh_token,
        "token_type": "Bearer",
        "expires_in": 7200
    }), 200

