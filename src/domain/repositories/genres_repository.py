from abc import ABC, abstractmethod
from typing import List, Tuple

class GenresRepository(ABC):

    @abstractmethod
    def count_most_watched_genres(self) -> List[Tuple[str, int]]:
        pass

    @abstractmethod
    def count_genres_da_hora(self) -> List[Tuple[str, int]]:
        pass

    @abstractmethod
    def count_genres_lixo(self) -> List[Tuple[str, int]]:
        pass

    @abstractmethod
    def count_genres_by_user(self, user_id: str) -> List[Tuple[str, int]]:
        pass