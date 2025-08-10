from typing import List, Optional

from models.movie import Movie
from src.domain.providers.connection_provider import ConnectionProvider
from src.domain.repositories.movies_repository import MoviesRepository

class MovieRepositoryPostgres(MoviesRepository):

    def __init__(self, conn_provider: ConnectionProvider):
        self.conn_provider = conn_provider

    def add_movie(self, title: str, responsible_id: str, spreadsheet_row: int,
                  genre: str, year: int, tmdb_id: int, poster_path: str) -> Optional[Movie]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            # No PostgreSQL, usamos RETURNING para pegar o id gerado
            cursor.execute("""
                           INSERT INTO movies (title, responsible_id, spreadsheet_row, genre, year, tmdb_id, poster_path, date_added)
                           VALUES (%s, %s, %s, %s, %s, %s, %s, NOW())
                               RETURNING id
                           """, (title, responsible_id, spreadsheet_row, genre, year, tmdb_id, poster_path))
            movie_id = cursor.fetchone()[0]
            conn.commit()

            cursor.execute("""
                           SELECT id, title, responsible_id, spreadsheet_row, genre, year, tmdb_id, poster_path, date_added
                           FROM movies WHERE id = %s
                           """, (movie_id,))
            row = cursor.fetchone()

            if row:
                return Movie(
                    id=row[0],
                    title=row[1],
                    responsible_id=row[2],
                    spreadsheet_row=row[3],
                    genre=row[4],
                    year=row[5],
                    tmdb_id=row[6],
                    poster_path=row[7],
                    date_added=row[8],
                )
            else:
                return None

    def find_movies_by_user(self, discord_id: str) -> List[Movie]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM movies WHERE responsible_id = %s", (discord_id,))
            rows = cursor.fetchall()

            return [
                Movie(
                    id=row[0],
                    title=row[1],
                    responsible_id=row[2],
                    spreadsheet_row=row[3],
                    genre=row[4],
                    year=row[5],
                    tmdb_id=row[6],
                    poster_path=row[7],
                    date_added=row[8],
                ) for row in rows
            ]

    def find_movie_by_row_and_user(self, responsible_id: str, spreadsheet_row: int) -> Optional[Movie]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT id, title, responsible_id, spreadsheet_row, genre, year, tmdb_id, poster_path, date_added FROM movies
                           WHERE responsible_id = %s AND spreadsheet_row = %s
                           """, (responsible_id, spreadsheet_row))
            row = cursor.fetchone()
            if row:
                return Movie(
                    id=row[0],
                    title=row[1],
                    responsible_id=row[2],
                    spreadsheet_row=row[3],
                    genre=row[4],
                    year=row[5],
                    tmdb_id=row[6],
                    poster_path=row[7],
                    date_added=row[8]
                )
            return None

    def find_movie_by_id(self, movie_id: int) -> Optional[Movie]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM movies WHERE id = %s", (movie_id,))
            row = cursor.fetchone()
            if row:
                return Movie(
                    id=row[0],
                    title=row[1],
                    responsible_id=row[2],
                    spreadsheet_row=row[3],
                    genre=row[4],
                    year=row[5],
                    tmdb_id=row[6],
                    poster_path=row[7],
                    date_added=row[8]
                )
            return None

    def find_all(self) -> List[Movie]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("SELECT * FROM movies ORDER BY responsible_id, spreadsheet_row")
            rows = cursor.fetchall()

            return [
                Movie(
                    id=row[0],
                    title=row[1],
                    responsible_id=row[2],
                    spreadsheet_row=row[3],
                    genre=row[4],
                    year=row[5],
                    tmdb_id=row[6],
                    poster_path=row[7],
                    date_added=row[8],
                ) for row in rows
            ]
