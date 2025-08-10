import os
from dotenv import load_dotenv

load_dotenv()

from src.domain.providers.connection_provider import ConnectionProvider
from src.infra.sqlite.sqlite_connection_provider import SQLiteConnectionProvider
from infra.postgres.postgres_connection_provider import PostgresConnectionProvider

CONNECTIONS = {
    "sqlite": SQLiteConnectionProvider,
    "postgres": PostgresConnectionProvider,
}

def get_connection_provider() -> ConnectionProvider:
    backend = os.getenv("DB_BACKEND").lower()
    try:
        return CONNECTIONS[backend]()
    except KeyError:
        raise ValueError(f"ConnectionProvider não suportado: {backend}")
