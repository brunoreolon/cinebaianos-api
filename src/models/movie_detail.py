class MovieDetail:
    def __init__(self, id, title, genre, year):
        self.id = id
        self.title = title
        self.genre = genre
        self.year = year

    def to_dict(self):
        return {
            "id": self.id,
            "title": self.title,
            "genres": self.genre,
            "year": self.year,
        }