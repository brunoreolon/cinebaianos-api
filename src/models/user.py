class User:
    def __init__(self, discord_id, name, tab, column, email, password):
        self.discord_id = discord_id
        self.name = name
        self.tab = tab
        self.column = column
        self.email = email
        self.password = password

    def to_dict(self):
        return {
            "discord_id": self.discord_id,
            "name": self.name,
            "tab": self.tab,
            "column": self.column,
            "email": self.email
        }

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            discord_id=data["discord_id"],
            name=data["name"],
            tab=data["tab"],
            column=data["column"],
            email=data["email"]
        )