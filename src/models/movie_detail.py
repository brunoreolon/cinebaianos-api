class MovieDetail:
    def __init__(self, id, title, genre, poster_path, year):
        self.id = id
        self.title = title
        self.genre = genre
        self.poster_path = poster_path
        self.year = year

    def to_dict(self):
        return {
            "id": self.id,
            "title": self.title,
            "genres": self.genre,
            "poster_path": self.poster_path,
            "year": self.year,
        }