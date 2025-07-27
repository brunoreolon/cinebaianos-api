import sqlite3
import os
from pathlib import Path

from src.domain.providers.connection_provider import ConnectionProvider

class SQLiteConnectionProvider(ConnectionProvider):
    def __init__(self, db_path=None):
        root_dir = Path(__file__).resolve().parents[3]
        db_path = root_dir / "data" / "filmes.db"
        db_path.parent.mkdir(parents=True, exist_ok=True)
        self.db_path = str(db_path)

    def get_connection(self):
        os.makedirs(os.path.dirname(self.db_path), exist_ok=True)
        return sqlite3.connect(self.db_path, check_same_thread=False)
