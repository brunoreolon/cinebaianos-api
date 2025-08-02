from typing import List, Tuple

from src.domain.providers.connection_provider import ConnectionProvider
from src.domain.repositories.votes_repository import VotesRepository

class VoteRepositorySQLite(VotesRepository):

    def __init__(self,  conn_provider: ConnectionProvider):
        self.conn_provider = conn_provider

    def register_vote(self, movie_id: int, responsible_id: str, voter_id: str, vote: int) -> None:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           INSERT INTO votes (movie_id, responsible_id, voter_id, vote)
                           VALUES (?, ?, ?, ?)
                               ON CONFLICT(movie_id, voter_id) DO UPDATE SET vote=excluded.vote
                           """, (movie_id, responsible_id, voter_id, vote))
            conn.commit()

    def count_votes_received_from_all_users(self, discord_id: str, vote: int) -> int:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT COUNT(*)
                           FROM movies f
                                    JOIN votes v ON f.id = v.movie_id
                           WHERE f.responsible_id = ? AND v.vote = ?
                           """, (discord_id, vote))
            result = cursor.fetchone()
            return result[0] if result else 0

    def count_all_votes_by_user(self) -> List[Tuple[str, int, int]]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT u.name,
                                  SUM(CASE WHEN v.vote = 'DA HORA' THEN 1 ELSE 0 END) AS da_hora,
                                  SUM(CASE WHEN v.vote = 'LIXO' THEN 1 ELSE 0 END) AS lixo
                           FROM users u
                                    LEFT JOIN movies f ON u.discord_id = f.responsible_id
                                    LEFT JOIN votes v ON f.id = v.movie_id
                           GROUP BY u.name
                           ORDER BY da_hora DESC
                           """)
            return cursor.fetchall()
