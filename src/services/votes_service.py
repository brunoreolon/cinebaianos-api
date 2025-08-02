from di.repository_factory import create_votes_repository
from exception.vote_registration_error import VoteRegistrationError
from models.vote_type import VoteType
from services.sheet_service import add_vote_to_spreadsheet
from services.users_service import get_user


def register_vote_db(conn_provider, movie_id, responsible_id, voter_id, vote):
    vote_repo = create_votes_repository(conn_provider)
    vote_enum = VoteType(vote)
    vote_repo.register_vote(movie_id, responsible_id, voter_id, vote_enum.label())

    return True

def register_vote_spreadsheet(conn_provider, voter, movie, vote_enum):
    vote = vote_enum.label()

    row = movie.spreadsheet_row
    responsible_id = movie.responsible_id

    user_responsable = get_user(conn_provider, responsible_id)
    tab = user_responsable.tab

    success = add_vote_to_spreadsheet(tab, row, voter.column, vote)

    if not success:
        raise VoteRegistrationError("Não foi possível registrar o voto")

    return True
