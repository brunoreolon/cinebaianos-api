from abc import ABC, abstractmethod
from typing import List, Optional

from models.movie import Movie


class MoviesRepository(ABC):

    @abstractmethod
    def add_movie(self, title: str, responsible_id: str, spreadsheet_row: int,
                  genre: str, year: int, tmdb_id: int) -> Movie:
        pass

    @abstractmethod
    def find_movies_by_user(self, discord_id: str) -> List[Movie]:
        pass

    @abstractmethod
    def find_movie_by_row_and_user(self, responsible_id: str, spreadsheet_row: int) -> Optional[Movie]:
        pass

    @abstractmethod
    def find_movie_by_id(self, movie_id: int) -> Optional[Movie]:
        pass

    @abstractmethod
    def find_all(self) -> List[Movie]:
        pass