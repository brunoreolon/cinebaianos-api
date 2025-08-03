import jwt
import inspect
from flask import request, current_app, jsonify
from functools import wraps

def require_auth(func):
    @wraps(func)
    def wrapper(*args, **kwargs):
        bot_token = request.headers.get("X-Bot-Token")
        jwt_token = request.headers.get("Authorization")

        sig = inspect.signature(func)

        # --- Verificação via BOT ---
        if bot_token and bot_token == current_app.config.get("BOT_API_TOKEN"):
            json_data = request.get_json(silent=True) or {}
            discord_id = json_data.get("discord_id") or json_data.get("voter_id")

            if "discord_id" in sig.parameters and not kwargs.get("discord_id"):
                kwargs["discord_id"] = discord_id

            return func(*args, **kwargs)

        # --- Verificação via Usuário (JWT) ---
        if jwt_token and jwt_token.startswith("Bearer "):
            token = jwt_token.split(" ")[1]
            try:
                payload = jwt.decode(
                    token,
                    current_app.config["JWT_SECRET"],
                    algorithms=["HS256"]
                )

                discord_id = payload.get("discord_id")

                if "discord_id" in sig.parameters and not kwargs.get("discord_id"):
                    kwargs["discord_id"] = discord_id

                return func(*args, **kwargs)

            except jwt.ExpiredSignatureError:
                return jsonify({"error": "Token expirado"}), 401
            except jwt.InvalidTokenError:
                return jsonify({"error": "Token inválido"}), 401

        return jsonify({"error": "Autenticação necessária"}), 401

    return wrapper
