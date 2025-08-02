from typing import List, Tuple

from src.domain.providers.connection_provider import ConnectionProvider
from src.domain.repositories.votes_repository import VotesRepository
from models.user import User

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

    def count_all_votes_per_user(self) -> List[Tuple[User, int, int]]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT u.discord_id, u.name, u.tab, u.column,
                                  SUM(CASE WHEN v.vote = 'DA HORA' THEN 1 ELSE 0 END) AS da_hora,
                                  SUM(CASE WHEN v.vote = 'LIXO' THEN 1 ELSE 0 END) AS lixo
                           FROM users u
                                    LEFT JOIN movies f ON u.discord_id = f.responsible_id
                                    LEFT JOIN votes v ON f.id = v.movie_id
                           GROUP BY u.discord_id, u.name, u.tab, u.column
                           ORDER BY da_hora DESC
                           """)
            rows = cursor.fetchall()

            result = []
            for row in rows:
                user = User(discord_id=row[0], name=row[1], tab=row[2], column=row[3])
                da_hora_count = row[4] or 0
                lixo_count = row[5] or 0
                result.append((user, da_hora_count, lixo_count))

            return result

    def count_da_hora_votes_per_user(self) -> List[Tuple[User, int]]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT u.discord_id, u.name, u.tab, u.column, COUNT(*) AS total
                           FROM users u
                                    LEFT JOIN movies f ON u.discord_id = f.responsible_id
                                    LEFT JOIN votes v ON f.id = v.movie_id
                           WHERE v.vote = 'DA HORA'
                           GROUP BY u.discord_id, u.name, u.tab, u.column
                           ORDER BY total DESC
                           """)
            rows = cursor.fetchall()

            result = []
            for row in rows:
                user = User(discord_id=row[0], name=row[1], tab=row[2], column=row[3])
                da_hora_count = row[4] or 0
                result.append((user, da_hora_count))

            return result

    def count_da_hora_votes_for_user(self, discord_id: str) -> int:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT COUNT(v.vote)
                           FROM users u
                                    JOIN movies f ON u.discord_id = f.responsible_id
                                    JOIN votes v ON f.id = v.movie_id
                           WHERE u.discord_id = ? AND v.vote = 'DA HORA'
                           """, (discord_id,))
            row = cursor.fetchone()

            return row[0] if row and row[0] else 0

    def count_lixo_votes_per_user(self) -> List[Tuple[User, int]]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT u.discord_id, u.name, u.tab, u.column, COUNT(*) AS total
                           FROM users u
                                    LEFT JOIN movies f ON u.discord_id = f.responsible_id
                                    LEFT JOIN votes v ON f.id = v.movie_id
                           WHERE v.vote = 'LIXO'
                           GROUP BY u.discord_id, u.name, u.tab, u.column
                           ORDER BY total DESC
                           """)
            rows = cursor.fetchall()

            result = []
            for row in rows:
                user = User(discord_id=row[0], name=row[1], tab=row[2], column=row[3])
                da_hora_count = row[4] or 0
                result.append((user, da_hora_count))

            return result

    def count_lixo_votes_for_user(self, discord_id: str) -> int:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT COUNT(v.vote)
                           FROM users u
                                    JOIN movies f ON u.discord_id = f.responsible_id
                                    JOIN votes v ON f.id = v.movie_id
                           WHERE u.discord_id = ? AND v.vote = 'LIXO'
                           """, (discord_id,))
            result = cursor.fetchone()

            return result[0] if result and result[0] else 0