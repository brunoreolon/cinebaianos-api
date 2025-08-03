class Movie:
    def __init__(self, id, title, responsible_id, spreadsheet_row, genre, year, tmdb_id, poster_path, date_added):
        self.id = id
        self.title = title
        self.responsible_id = responsible_id
        self.spreadsheet_row = spreadsheet_row
        self.genre = genre
        self.year = year
        self.tmdb_id = tmdb_id
        self.poster_path = poster_path
        self.date_added = date_added

    def to_dict(self):
        return {
            "id": self.id,
            "title": self.title,
            "spreadsheet_row": self.spreadsheet_row,
            "genre": self.genre,
            "year": self.year,
            "tmdb_id": self.tmdb_id,
            "poster_path": self.poster_path,
            "date_added": self.date_added,
        }