from di.repository_factory import create_users_repository

def create_user(conn_provider, discord_id, name, tab, column):
    user_repo = create_users_repository(conn_provider)
    existing_user = user_repo.get_user(discord_id)

    if existing_user:
        raise ValueError("Usuário já cadastrado.")

    user = user_repo.register_user(discord_id, name, tab, column)

    return user

def get_user(conn_provider, discord_id):
    user_repo = create_users_repository(conn_provider)
    user = user_repo.get_user(discord_id)

    if not user:
        raise ValueError("Usuário não encontrado")

    return user

def get_all(conn_provider):
    user_repo = create_users_repository(conn_provider)
    users = user_repo.get_all_users()

    return users