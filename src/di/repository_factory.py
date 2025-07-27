import os
from dotenv import load_dotenv

from src.domain.repositories.users_repository import UsersRepository
from src.domain.repositories.movies_repository import MoviesRepository
from src.domain.repositories.votes_repository import VotesRepository
from src.domain.repositories.genres_repository import GenresRepository

from src.infra.sqlite.users_repository_sqlite import UserRepositorySQLite
from src.infra.sqlite.movies_repository_sqlite import MovieRepositorySQLite
from src.infra.sqlite.votes_repository_sqlite import VoteRepositorySQLite
from src.infra.sqlite.genres_repository_sqlite import GenresRepositorySQLite

load_dotenv()

# Registrando os backends suportados
_USERS_REPOSITORIES = {
    "sqlite": UserRepositorySQLite,
}

_MOVIES_REPOSITORIES = {
    "sqlite": MovieRepositorySQLite,
}

_VOTES_REPOSITORIES = {
    "sqlite": VoteRepositorySQLite,
}

_GENRES_REPOSITORIES = {
    "sqlite": GenresRepositorySQLite,
}

def create_users_repository(conn_provider) -> UsersRepository:
    backend = os.getenv("DB_BACKEND").lower()
    try:
        return _USERS_REPOSITORIES[backend](conn_provider)
    except KeyError:
        raise ValueError(f"Backend '{backend}' não suportado para UsuariosRepository.")

def create_movies_repository(conn_provider) -> MoviesRepository:
    backend = os.getenv("DB_BACKEND").lower()
    try:
        return _MOVIES_REPOSITORIES[backend](conn_provider)
    except KeyError:
        raise ValueError(f"Backend '{backend}' não suportado para FilmesRepository.")

def create_votes_repository(conn_provider) -> VotesRepository:
    backend = os.getenv("DB_BACKEND").lower()
    try:
        return _VOTES_REPOSITORIES[backend](conn_provider)
    except KeyError:
        raise ValueError(f"Backend '{backend}' não suportado para VotosRepository.")

def create_genres_repository(conn_provider) -> GenresRepository:
    backend = os.getenv("DB_BACKEND").lower()
    try:
        return _GENRES_REPOSITORIES[backend](conn_provider)
    except KeyError:
        raise ValueError(f"Backend '{backend}' não suportado para GenerosRepository.")