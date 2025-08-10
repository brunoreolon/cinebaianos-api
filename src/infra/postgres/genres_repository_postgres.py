from typing import List, Tuple

from src.domain.providers.connection_provider import ConnectionProvider
from src.domain.repositories.genres_repository import GenresRepository
from models.vote_type import VoteType

class GenresRepositoryPostgres(GenresRepository):

    def __init__(self, conn_provider: ConnectionProvider):
        self.conn_provider = conn_provider

    def count_most_watched_genres(self) -> List[Tuple[str, int]]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT genre FROM movies")
            rows = cursor.fetchall()

        return self._count_genders_from_lines(rows)

    def count_genres_da_hora(self) -> List[Tuple[str, int]]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT f.genre
                           FROM votes v
                                    JOIN movies f ON v.movie_id = f.id
                           WHERE v.vote = %s
                           """, (VoteType.DA_HORA.value,))
            rows = cursor.fetchall()

        return self._count_genders_from_lines(rows)

    def count_genres_lixo(self) -> List[Tuple[str, int]]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT f.genre
                           FROM votes v
                                    JOIN movies f ON v.movie_id = f.id
                           WHERE v.vote = %s
                           """, (VoteType.LIXO.value,))
            rows = cursor.fetchall()

        return self._count_genders_from_lines(rows)

    def count_genres_by_user(self, user_id: str) -> List[Tuple[str, int]]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT genre
                           FROM movies
                           WHERE responsible_id = %s
                           """, (user_id,))
            rows = cursor.fetchall()

        return self._count_genders_from_lines(rows)

    def _count_genders_from_lines(self, rows: List[Tuple[str]]) -> List[Tuple[str, int]]:
        count = {}
        for row in rows:
            if not row[0]:
                continue
            genres = [g.strip() for g in row[0].split(",")]
            for genre in genres:
                count[genre] = count.get(genre, 0) + 1

        return sorted(count.items(), key=lambda x: x[1], reverse=True)
