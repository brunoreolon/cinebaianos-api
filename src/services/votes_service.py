from exception.user_not_found_error import UserNotFoundError
from exception.user_voter_not_found_error import UserVoterNotFoundError
from exception.vote_registration_error import VoteRegistrationError
from services.movies_service import get_movie_by_id
from services.sheet_service import add_vote_to_spreadsheet
from services.users_service import get_user


def register_vote(conn_provider, voter_id, movie_id, vote_enum):
    vote = vote_enum.label()

    movie = get_movie_by_id(conn_provider, movie_id)
    row = movie.spreadsheet_row
    responsible_id = movie.responsible_id

    user_responsable = get_user(conn_provider, responsible_id)
    tab = user_responsable.tab

    try:
        user_voter = get_user(conn_provider, voter_id)
        column = user_voter.column
    except UserNotFoundError:
        raise UserVoterNotFoundError(f"Usuário votante com ID '{voter_id}' não encontrado.")

    success = add_vote_to_spreadsheet(tab, row, column, vote)

    if not success:
        raise VoteRegistrationError("Não foi possível registrar o voto")

    return True