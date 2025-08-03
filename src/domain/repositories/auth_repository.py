from abc import ABC, abstractmethod
from datetime import datetime
from typing import Optional

class AuthRepository(ABC):
    @abstractmethod
    def save(self, token: str, discord_id: str, expires_at: datetime) -> None:
        pass

    @abstractmethod
    def get(self, token: str) -> Optional[tuple[str, datetime]]:
        pass

    @abstractmethod
    def delete(self, token: str) -> None:
        pass
