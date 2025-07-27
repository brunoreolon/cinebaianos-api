class User:
    def __init__(self, discord_id, name, tab, column):
        self.discord_id = discord_id
        self.name = name
        self.tab = tab
        self.column = column

    def to_dict(self):
        return {
            "discord_id": self.discord_id,
            "name": self.name,
            "tab": self.tab,
            "column": self.column
        }

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            discord_id=data["discord_id"],
            name=data["name"],
            tab=data["tab"],
            column=data["column"]
        )