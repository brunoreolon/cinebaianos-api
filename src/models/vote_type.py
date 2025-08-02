from enum import IntEnum

from exception.invalid_vote_error import InvalidVoteError


class VoteType(IntEnum):
    DA_HORA = 1
    LIXO = 2
    NAO_ASSISTI = 3

    def label(self) -> str:
        try:
            return {
                VoteType.DA_HORA: "DA HORA",
                VoteType.LIXO: "LIXO",
                VoteType.NAO_ASSISTI: "NÃO ASSISTI"
            }[self]
        except ValueError:
            raise InvalidVoteError(f"Voto inválido")

    @classmethod
    def from_value(cls, value: int) -> "VoteType":
        try:
            return cls(value)
        except ValueError:
            raise InvalidVoteError(f"Voto inválido: {value}")

    @classmethod
    def name_from_value(cls, value: int) -> str:
        try:
            return cls(value).name.replace("_", " ").title()
        except ValueError:
            raise InvalidVoteError(f"Voto inválido: {value}")
