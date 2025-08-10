import psycopg2

from config import Config
from src.domain.providers.connection_provider import ConnectionProvider

class PostgresConnectionProvider(ConnectionProvider):
    def __init__(self):
        required_vars = ["DB_USER", "DB_PASSWORD", "DB_HOST", "DB_PORT", "DB_NAME"]
        for var in required_vars:
            if not getattr(Config, var, None):
                raise ValueError(f"Variável de ambiente {var} não configurada")

    def get_connection(self):
        conn = psycopg2.connect(
            user=Config.DB_USER,
            password=Config.DB_PASSWORD,
            host=Config.DB_HOST,
            port=Config.DB_PORT,
            database=Config.DB_NAME,
        )

        return conn
