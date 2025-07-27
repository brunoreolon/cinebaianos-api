from abc import ABC, abstractmethod

class SchemasRepository(ABC):

    @abstractmethod
    def create_tables(self):
        pass