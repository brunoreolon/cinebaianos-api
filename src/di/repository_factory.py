import os
from dotenv import load_dotenv

from src.domain.repositories.usuarios_repository import UsuariosRepository
from src.domain.repositories.filmes_repository import FilmesRepository
from src.domain.repositories.votos_repository import VotosRepository
from src.domain.repositories.generos_repository import GenerosRepository

from src.infra.sqlite.usuarios_repository_sqlite import UsuarioRepositorySQLite
from src.infra.sqlite.filmes_repository_sqlite import FilmesRepositorySQLite
from src.infra.sqlite.votos_repository_sqlite import VotosRepositorySQLite
from src.infra.sqlite.generos_repository_sqlite import GenerosRepositorySQLite

load_dotenv()

# Registrando os backends suportados
_REPOSITORIOS_USUARIOS = {
    "sqlite": UsuarioRepositorySQLite,
}

_REPOSITORIOS_FILMES = {
    "sqlite": FilmesRepositorySQLite,
}

_REPOSITORIOS_VOTOS = {
    "sqlite": VotosRepositorySQLite,
}

_REPOSITORIOS_GENEROS = {
    "sqlite": GenerosRepositorySQLite,
}

def criar_usuarios_repository(conn_provider) -> UsuariosRepository:
    backend = os.getenv("DB_BACKEND").lower()
    try:
        return _REPOSITORIOS_USUARIOS[backend](conn_provider)
    except KeyError:
        raise ValueError(f"Backend '{backend}' não suportado para UsuariosRepository.")

def criar_filmes_repository(conn_provider) -> FilmesRepository:
    backend = os.getenv("DB_BACKEND").lower()
    try:
        return _REPOSITORIOS_FILMES[backend](conn_provider)
    except KeyError:
        raise ValueError(f"Backend '{backend}' não suportado para FilmesRepository.")

def criar_votos_repository(conn_provider) -> VotosRepository:
    backend = os.getenv("DB_BACKEND").lower()
    try:
        return _REPOSITORIOS_VOTOS[backend](conn_provider)
    except KeyError:
        raise ValueError(f"Backend '{backend}' não suportado para VotosRepository.")

def criar_generos_repository(conn_provider) -> GenerosRepository:
    backend = os.getenv("DB_BACKEND").lower()
    try:
        return _REPOSITORIOS_GENEROS[backend](conn_provider)
    except KeyError:
        raise ValueError(f"Backend '{backend}' não suportado para GenerosRepository.")