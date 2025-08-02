from pathlib import Path
from src.domain.repositories.schemas_repository import SchemasRepository

class SchemasRepositorySQLite(SchemasRepository):

    def __init__(self, conn_provider):
        file_path = Path(__file__).resolve()
        base_dir = file_path.parents[3]
        self.db_path = base_dir / "db.sqlite"
        self.conn_provider = conn_provider

    def create_tables(self):
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()

            cursor.execute("""
                           CREATE TABLE IF NOT EXISTS users (
                                                                   discord_id TEXT PRIMARY KEY,
                                                                   name TEXT,
                                                                   tab TEXT,
                                                                   column TEXT
                           )""")

            cursor.execute("""
                           CREATE TABLE IF NOT EXISTS movies (
                                                                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                                 title TEXT,
                                                                 responsible_id TEXT,
                                                                 spreadsheet_row INTEGER,
                                                                 genre TEXT,
                                                                 year INTEGER,
                                                                 tmdb_id INTEGER,
                                                                 date_added TEXT,
                                                                 FOREIGN KEY(responsible_id) REFERENCES users(discord_id)
                               )""")

            cursor.execute("""
                           CREATE TABLE IF NOT EXISTS votes (
                                                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                                movie_id INTEGER,
                                                                responsible_id TEXT,
                                                                voter_id TEXT,
                                                                vote INTEGER,
                                                                FOREIGN KEY(movie_id) REFERENCES movies(id),
                               FOREIGN KEY(responsible_id) REFERENCES users(discord_id),
                               FOREIGN KEY(voter_id) REFERENCES users(discord_id),
                               UNIQUE(movie_id, voter_id)
                               )""")

            conn.commit()
