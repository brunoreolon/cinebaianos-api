import os
from dotenv import load_dotenv


load_dotenv()

from src.domain.repositories.schemas_repository import SchemasRepository
from src.infra.sqlite.schema_repository_sqlite import SchemasRepositorySQLite
from infra.postgres.schema_repository_postgres import SchemasRepositoryPostgres

SCHEMAS = {
    "sqlite": SchemasRepositorySQLite,
    "postgres": SchemasRepositoryPostgres,
}

def create_schemas_repository(conn_provider) -> SchemasRepository:
    backend = os.getenv("DB_BACKEND").lower()

    try:
        return SCHEMAS[backend](conn_provider)
    except KeyError:
        raise ValueError(f"Schema para backend '{backend}' não suportado.")
