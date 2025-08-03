from typing import List

from models.user import User
from src.domain.providers.connection_provider import ConnectionProvider
from src.domain.repositories.users_repository import UsersRepository

class UserRepositorySQLite(UsersRepository):

    def __init__(self, conn_provider: ConnectionProvider):
        self.conn_provider = conn_provider

    def register_user(self, discord_id: str, name: str, tab: str, column: str, email: str, password: str) -> User:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                INSERT OR REPLACE INTO users (discord_id, name, tab, column, email, password)
                VALUES (?, ?, ?, ?, ?, ?)
            """, (discord_id, name, tab, column, email, password))
            conn.commit()
            cursor.execute("""
                           SELECT discord_id, name, tab, column, email, password FROM users WHERE discord_id = ?
                           """, (discord_id,))
            row = cursor.fetchone()
            if row:
                return User(
                    discord_id=row[0],
                    name=row[1],
                    tab=row[2],
                    column=row[3],
                    email=row[4],
                    password=row[5]
                )
            else:
                return None

    def get_all_users(self)-> List[User]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM users")
            rows = cursor.fetchall()

            users = []
            if rows:
                users = [
                    User(
                        discord_id=row[0],
                        name=row[1],
                        tab=row[2],
                        column=row[3],
                        email=row[4],
                        password=row[5]
                    )
                    for row in rows
                ]

            return users


    def get_user(self, discord_id: str)-> User:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT discord_id, name, tab, column, email, password
                           FROM users
                           WHERE discord_id = ?
                           """, (discord_id,))
            row = cursor.fetchone()

            if row:
                return User(
                    discord_id=row[0],
                    name=row[1],
                    tab=row[2],
                    column=row[3],
                    email=row[4],
                    password=row[5]
                )

            return None

    def get_user_by_email(self, email: str)-> User:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT *
                           FROM users
                           WHERE email = ?
                           """, (email,))
            row = cursor.fetchone()

            if row:
                return User(
                    discord_id=row[0],
                    name=row[1],
                    tab=row[2],
                    column=row[3],
                    email=row[4],
                    password=row[5]
                )

            return None
