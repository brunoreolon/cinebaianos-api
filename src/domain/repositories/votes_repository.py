from abc import ABC, abstractmethod
from typing import List, Tuple

class VotesRepository(ABC):

    @abstractmethod
    def register_vote(self, movie_id: int, responsible_id: str, voter_id: str, vote: str) -> None:
        pass

    @abstractmethod
    def count_votes_received_from_all_users(self, discord_id: str, type_vote: str) -> int:
        pass

    @abstractmethod
    def count_all_votes_by_user(self) -> List[Tuple[str, int, int]]:
        pass
