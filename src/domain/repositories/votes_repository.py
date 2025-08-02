from abc import ABC, abstractmethod
from typing import List, Tuple

from models.user import User


class VotesRepository(ABC):

    @abstractmethod
    def register_vote(self, movie_id: int, responsible_id: str, voter_id: str, vote: int) -> None:
        pass

    @abstractmethod
    def count_all_votes_per_user(self) -> List[Tuple[User, int, int]]:
        pass

    @abstractmethod
    def count_da_hora_votes_per_user(self) -> List[Tuple[User, int]]:
        pass

    @abstractmethod
    def count_da_hora_votes_for_user(self, discord_id: str) -> int:
        pass

    @abstractmethod
    def count_lixo_votes_per_user(self) -> List[Tuple[User, int]]:
        pass

    @abstractmethod
    def count_lixo_votes_for_user(self, discord_id: str) -> int:
        pass