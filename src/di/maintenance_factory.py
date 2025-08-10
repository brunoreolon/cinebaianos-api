import os
from dotenv import load_dotenv


load_dotenv()

from src.domain.repositories.maintenance_repository import MaintenanceRepository
from src.infra.sqlite.maintenance_repository_sqlite import MaintenanceRepositorySQLite
from infra.postgres.maintenance_repository_postgres import MaintenanceRepositoryPostgres

REPOSITORIOS_MAINTENANCE = {
    "sqlite": MaintenanceRepositorySQLite,
    "postgres": MaintenanceRepositoryPostgres,
}

def create_maintenance_repository(conn_provider)-> MaintenanceRepository:
    backend = os.getenv("DB_BACKEND").lower()

    try:
        return REPOSITORIOS_MAINTENANCE[backend](conn_provider)
    except KeyError:
        raise ValueError(f"Backend '{backend}' não suportado para MaintenanceRepository.")