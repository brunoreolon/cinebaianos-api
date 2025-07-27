from abc import ABC, abstractmethod

class MaintenanceRepository(ABC):

    @abstractmethod
    def clear_movie_bank(self):
        pass