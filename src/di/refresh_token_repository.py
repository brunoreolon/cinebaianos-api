import os

from domain.repositories.auth_repository import AuthRepository
from infra.sqlite.auth_repository_sqlite import AuthRepositorySQLite

_REFRESH_TOKEN_REPOSITORIES = {
    "sqlite": AuthRepositorySQLite,
}

def create_refresh_token_repository(conn_provider) -> AuthRepository:
    backend = os.getenv("DB_BACKEND").lower()
    try:
        return _REFRESH_TOKEN_REPOSITORIES[backend](conn_provider)
    except KeyError:
        raise ValueError(f"Backend '{backend}' não suportado para GenerosRepository.")