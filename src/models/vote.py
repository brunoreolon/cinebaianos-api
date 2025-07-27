class Vote:
    def __init__(self, id, movie_id, responsible_id, voter_id, vote):
        self.id = id
        self.movie_id = movie_id
        self.responsible_id = responsible_id
        self.voter_id = voter_id
        self.vote = vote

    def to_dict(self):
        return {
            "id": self.discord_id,
            "movie_id": self.movie_id,
            "responsible_id": self.responsible_id,
            "voter_id": self.voter_id,
            "vote": self.vote
        }

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            id=data["id"],
            movie_id=data["movie_id"],
            responsible_id=data["responsible_id"],
            voter_id=data["voter_id"],
            vote=data["vote"]
        )