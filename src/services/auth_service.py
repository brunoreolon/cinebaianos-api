import jwt
import datetime
from werkzeug.security import check_password_hash
from flask import current_app
from datetime import datetime, timedelta
import secrets

from di.refresh_token_repository import create_refresh_token_repository
from exception.authentication_error import AuthenticationError
from exception.user_not_found_error import UserNotFoundError
from services.users_service import get_user_by_email


def authenticate_user(conn_provider, email, password):
    user = get_user_by_email(conn_provider, email)

    if not user:
        raise UserNotFoundError("Usuário não encontrado")

    if not check_password_hash(user.password, password):
        raise AuthenticationError("Senha incorreta")

    payload = {
        "discord_id": user.discord_id,
        "name": user.name,
        "exp": datetime.utcnow() + timedelta(hours=2)
    }

    token = jwt.encode(payload, current_app.config["JWT_SECRET"], algorithm="HS256")
    return token, user.discord_id

def generate_and_save_refresh_token(conn_provider, discord_id: str):
    auth_repo = create_refresh_token_repository(conn_provider)

    token = secrets.token_hex(32)
    expires_at = datetime.utcnow() + timedelta(days=7)
    auth_repo.save(token, discord_id, expires_at)
    return token

def save(conn_provider, token, discord_id, expires_at):
    auth_repo = create_refresh_token_repository(conn_provider)
    auth_repo.save(token, discord_id, expires_at)

def delete(conn_provider, token):
    auth_repo = create_refresh_token_repository(conn_provider)
    auth_repo.delete(token)

def get(conn_provider, token):
    auth_repo = create_refresh_token_repository(conn_provider)
    return auth_repo.get(token)

