from typing import List, Optional

from models.movie import Movie
from src.domain.providers.connection_provider import ConnectionProvider
from src.domain.repositories.movies_repository import MoviesRepository

class MovieRepositorySQLite(MoviesRepository):

    def __init__(self, conn_provider: ConnectionProvider):
        self.conn_provider = conn_provider

    def add_movie(self, title: str, responsible_id: str, spreadsheet_row: int,
                  genre: str, year: int, tmdb_id: int, poster_path: str) -> Movie:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           INSERT INTO movies (title, responsible_id, spreadsheet_row, genre, year, tmdb_id, poster_path, date_added)
                           VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'))
                           """, (title, responsible_id, spreadsheet_row, genre, year, tmdb_id, poster_path))
            conn.commit()
            movie_id = cursor.lastrowid

            cursor.execute("""
                           SELECT id, title, responsible_id, spreadsheet_row, genre, year, tmdb_id, poster_path, 
                               date_added FROM movies WHERE id = ?
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
            cursor.execute("SELECT * FROM movies WHERE responsible_id = ?", (discord_id,))
            rows = cursor.fetchall()

            movies = []
            if rows:
                movies = [
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
                    )
                    for row in rows
                ]

            return movies


    def find_movie_by_row_and_user(self, responsible_id: str, spreadsheet_row: int) -> Optional[Movie]:
        with self.conn_provider.get_connection() as conn:
            cursor = conn.cursor()
            cursor.execute("""
                           SELECT id, title, responsible_id, spreadsheet_row, genre, year, tmdb_id, poster_path, date_added FROM movies
                           WHERE responsible_id = ? AND spreadsheet_row = ?
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
            cursor.execute("SELECT * FROM movies WHERE id = ?", (movie_id,))
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

            movies = []
            if rows:
                movies = [
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
                    )
                    for row in rows
                ]

            return movies