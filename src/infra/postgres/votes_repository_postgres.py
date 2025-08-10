from typing import List, Tuple

from src.domain.providers.connection_provider import ConnectionProvider
from src.domain.repositories.votes_repository import VotesRepository
from models.user import User
from models.vote_type import VoteType


class VoteRepositoryPostgres(VotesRepository):

    def __init__(self, conn_provider: ConnectionProvider):
        self.conn_provider = conn_provider

    def register_vote(self, movie_id: int, responsible_id: str, voter_id: str, vote: int) -> None:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           INSERT INTO votes (movie_id, responsible_id, voter_id, vote)
                           VALUES (%s, %s, %s, %s)
                               ON CONFLICT (movie_id, voter_id) DO UPDATE SET vote = EXCLUDED.vote
                           """, (movie_id, responsible_id, voter_id, vote))
            conn.commit()

    def count_all_votes_per_user(self) -> List[Tuple[User, int, int]]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(f"""
                SELECT u.discord_id, u.name, u.tab, u.col, email, password,
                       SUM(CASE WHEN v.vote = {VoteType.DA_HORA.value} THEN 1 ELSE 0 END) AS da_hora,
                       SUM(CASE WHEN v.vote = {VoteType.LIXO.value} THEN 1 ELSE 0 END) AS lixo
                FROM users u
                LEFT JOIN movies f ON u.discord_id = f.responsible_id
                LEFT JOIN votes v ON f.id = v.movie_id
                GROUP BY u.discord_id, u.name, u.tab, u.col, email, password
                ORDER BY da_hora DESC
            """)
            rows = cursor.fetchall()

            result = []
            for row in rows:
                user = User(discord_id=row[0], name=row[1], tab=row[2], column=row[3], email=row[4], password=row[5])
                da_hora_count = row[6] or 0
                lixo_count = row[7] or 0
                result.append((user, da_hora_count, lixo_count))

            return result

    def count_da_hora_votes_per_user(self) -> List[Tuple[User, int]]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(f"""
                SELECT u.discord_id, u.name, u.tab, u.col, email, password, COUNT(*) AS total
                FROM users u
                LEFT JOIN movies f ON u.discord_id = f.responsible_id
                LEFT JOIN votes v ON f.id = v.movie_id
                WHERE v.vote = {VoteType.DA_HORA.value}
                GROUP BY u.discord_id, u.name, u.tab, u.col, email, password
                ORDER BY total DESC
            """)
            rows = cursor.fetchall()

            result = []
            for row in rows:
                user = User(discord_id=row[0], name=row[1], tab=row[2], column=row[3], email=row[4], password=row[5])
                da_hora_count = row[6] or 0
                result.append((user, da_hora_count))

            return result

    def count_da_hora_votes_for_user(self, discord_id: str) -> int:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(f"""
                SELECT COUNT(v.vote)
                FROM users u
                JOIN movies f ON u.discord_id = f.responsible_id
                JOIN votes v ON f.id = v.movie_id
                WHERE u.discord_id = %s AND v.vote = {VoteType.DA_HORA.value}
            """, (discord_id,))
            row = cursor.fetchone()

            return row[0] if row and row[0] else 0

    def count_lixo_votes_per_user(self) -> List[Tuple[User, int]]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(f"""
                SELECT u.discord_id, u.name, u.tab, u.col, email, password, COUNT(*) AS total
                FROM users u
                LEFT JOIN movies f ON u.discord_id = f.responsible_id
                LEFT JOIN votes v ON f.id = v.movie_id
                WHERE v.vote = {VoteType.LIXO.value}
                GROUP BY u.discord_id, u.name, u.tab, u.col, email, password
                ORDER BY total DESC
            """)
            rows = cursor.fetchall()

            result = []
            for row in rows:
                user = User(discord_id=row[0], name=row[1], tab=row[2], column=row[3], email=row[4], password=row[5])
                lixo_count = row[6] or 0
                result.append((user, lixo_count))

            return result

    def count_lixo_votes_for_user(self, discord_id: str) -> int:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(f"""
                SELECT COUNT(v.vote)
                FROM users u
                JOIN movies f ON u.discord_id = f.responsible_id
                JOIN votes v ON f.id = v.movie_id
                WHERE u.discord_id = %s AND v.vote = {VoteType.LIXO.value}
            """, (discord_id,))
            result = cursor.fetchone()

            return result[0] if result and result[0] else 0
