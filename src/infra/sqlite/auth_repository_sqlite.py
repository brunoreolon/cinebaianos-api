from datetime import datetime

from domain.repositories.auth_repository import AuthRepository


class AuthRepositorySQLite(AuthRepository):

    def __init__(self, conn_provider):
        self.conn_provider = conn_provider

    def save(self, token, discord_id, expires_at):
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "INSERT INTO refresh_tokens (token, discord_id, expires_at) VALUES (?, ?, ?)",
                (token, discord_id, expires_at.strftime("%Y-%m-%d %H:%M:%S"))
            )
            conn.commit()

    def get(self, token):
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute(
                "SELECT discord_id, expires_at FROM refresh_tokens WHERE token = ?",
                (token,)
            )
            row = cursor.fetchone()
            if row:
                return row[0], datetime.strptime(row[1], "%Y-%m-%d %H:%M:%S")
            return None

    def delete(self, token):
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("DELETE FROM refresh_tokens WHERE token = ?", (token,))
            conn.commit()
