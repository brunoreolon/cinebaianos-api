from domain.repositories.maintenance_repository import MaintenanceRepository


class MaintenanceRepositoryPostgres(MaintenanceRepository):

    def __init__(self, conn_provider):
        self.conn_provider = conn_provider

    def clear_movie_bank(self):
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM votes")
            cursor.execute("DELETE FROM movies")
            # Substitua 'movies_id_seq' e 'votes_id_seq' pelo nome correto da sequence do seu banco
            cursor.execute("ALTER SEQUENCE movies_id_seq RESTART WITH 1")
            cursor.execute("ALTER SEQUENCE votes_id_seq RESTART WITH 1")
            conn.commit()
