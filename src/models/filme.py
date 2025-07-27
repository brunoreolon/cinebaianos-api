class Filme:
    def __init__(self, id, title, genres, poster_path, ano):
        self.id = id
        self.title = title
        self.genres = genres
        self.poster_path = poster_path
        self.ano = ano

    def to_dict(self):
        return {
            "id": self.id,
            "title": self.title,
            "genres": self.genres,
            "poster_path": self.poster_path,
            "ano": self.ano
        }