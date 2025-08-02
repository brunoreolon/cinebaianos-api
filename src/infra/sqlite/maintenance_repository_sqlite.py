from src.domain.repositories.maintenance_repository import MaintenanceRepository

class MaintenanceRepositorySQLite(MaintenanceRepository):

    def __init__(self, conn_provider):
        self.conn_provider = conn_provider

    def clear_movie_bank(self):
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM movies")
            cursor.execute("DELETE FROM votes")
            cursor.execute("DELETE FROM sqlite_sequence WHERE name = 'movies'")
            cursor.execute("DELETE FROM sqlite_sequence WHERE name = 'votes'")
            conn.commit()