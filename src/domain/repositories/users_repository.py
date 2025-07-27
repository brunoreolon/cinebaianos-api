from abc import ABC, abstractmethod
from typing import List, Optional

from models.user import User

class UsersRepository(ABC):

    @abstractmethod
    def register_user(self, discord_id: str, name: str, tab: str, column: str) -> User:
        pass

    @abstractmethod
    def get_all_users(self) -> List[User]:
        pass

    @abstractmethod
    def get_user(self, discord_id: str) -> Optional[User]:
        pass