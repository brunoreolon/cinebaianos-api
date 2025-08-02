from di.repository_factory import create_votes_repository
from models.user import User
from services.users_service import get_user


def get_ranking(conn_provider) -> list[dict]:
    vote_repo = create_votes_repository(conn_provider)

    ranking_data = vote_repo.count_all_votes_per_user()

    result = []
    for user, total_da_hora, total_lixo in ranking_data:
        result.append({
            "user": {
                "discord_id": user.discord_id,
                "name": user.name,
                "tab": user.tab,
                "column": user.column
            },
            "total_da_hora": total_da_hora,
            "total_lixo": total_lixo
        })

    return result

def get_da_hora_ranking(conn_provider) -> list[dict]:
    vote_repo = create_votes_repository(conn_provider)

    ranking_data = vote_repo.count_da_hora_votes_per_user()

    result = []
    for user, total_da_hora in ranking_data:
        result.append({
            "user": {
                "discord_id": user.discord_id,
                "name": user.name,
                "tab": user.tab,
                "column": user.column
            },
            "total_da_hora": total_da_hora
        })

    return result

def get_da_hora_total(conn_provider, discord_id) -> tuple[User, int]:
    vote_repo = create_votes_repository(conn_provider)

    user = get_user(conn_provider, discord_id)

    total = vote_repo.count_da_hora_votes_for_user(user.discord_id)

    return user, total or 0

def get_lixo_ranking(conn_provider) -> list[dict]:
    vote_repo = create_votes_repository(conn_provider)

    ranking_data = vote_repo.count_lixo_votes_per_user()

    result = []
    for user, total_lixo in ranking_data:
        result.append({
            "user": {
                "discord_id": user.discord_id,
                "name": user.name,
                "tab": user.tab,
                "column": user.column
            },
            "total_lixo": total_lixo
        })

    return result

def get_lixo_total(conn_provider, discord_id) -> tuple[User, int]:
    vote_repo = create_votes_repository(conn_provider)

    user = get_user(conn_provider, discord_id)

    total = vote_repo.count_lixo_votes_for_user(user.discord_id)

    return user, total or 0
