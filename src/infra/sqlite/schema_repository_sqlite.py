from pathlib import Path
from src.domain.repositories.schemas_repository import SchemasRepository

class SchemasRepositorySQLite(SchemasRepository):

    def __init__(self, conn_provider):
        file_path = Path(__file__).resolve()
        base_dir = file_path.parents[3]
        self.db_path = base_dir / "db.sqlite"
        self.conn_provider = conn_provider

    def criar_tabelas(self):
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()

            cursor.execute("""
                           CREATE TABLE IF NOT EXISTS usuarios (
                                                                   discord_id TEXT PRIMARY KEY,
                                                                   nome TEXT,
                                                                   aba TEXT,
                                                                   coluna TEXT
                           )""")

            cursor.execute("""
                           CREATE TABLE IF NOT EXISTS filmes (
                                                                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                                 titulo TEXT,
                                                                 id_responsavel TEXT,
                                                                 linha_planilha INTEGER,
                                                                 genero TEXT,
                                                                 ano INTEGER,
                                                                 tmdb_id INTEGER,
                                                                 data_adicionado TEXT,
                                                                 FOREIGN KEY(id_responsavel) REFERENCES usuarios(discord_id)
                               )""")

            cursor.execute("""
                           CREATE TABLE IF NOT EXISTS votos (
                                                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                                id_filme INTEGER,
                                                                id_responsavel TEXT,
                                                                id_votante TEXT,
                                                                voto TEXT,
                                                                FOREIGN KEY(id_filme) REFERENCES filmes(id),
                               FOREIGN KEY(id_responsavel) REFERENCES usuarios(discord_id),
                               FOREIGN KEY(id_votante) REFERENCES usuarios(discord_id),
                               UNIQUE(id_filme, id_votante)
                               )""")

            conn.commit()
