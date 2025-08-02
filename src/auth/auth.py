import functools
import inspect

from flask import request, current_app, abort
import jwt

def require_auth(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        bot_token = request.headers.get("X-Bot-Token")
        auth_header = request.headers.get("Authorization")

        # 1. Autenticação via bot token
        if bot_token and bot_token == current_app.config.get("BOT_API_TOKEN"):
            json_data = request.get_json(silent=True) or {}
            discord_id = json_data.get("discord_id") or json_data.get("voter_id")


            sig = inspect.signature(func)
            if (
                    "discord_id" in sig.parameters
                    and discord_id is not None
                    and kwargs.get("discord_id") is None
            ):
                kwargs["discord_id"] = discord_id

            return func(*args, **kwargs)

        # 2. Autenticação via JWT
        if auth_header and auth_header.startswith("Bearer "):
            token = auth_header.split(" ")[1]
            try:
                payload = jwt.decode(token, current_app.config["JWT_SECRET"], algorithms=["HS256"])
                discord_id = payload.get("discord_id")

                if "discord_id" not in kwargs:
                    kwargs["discord_id"] = discord_id

                return func(*args, **kwargs)

            except jwt.ExpiredSignatureError:
                abort(401, "Token JWT expirado")
            except jwt.InvalidTokenError:
                abort(401, "Token JWT inválido")

        # 3. Não autenticado
        abort(401, "Não autenticado")

    return wrapper
