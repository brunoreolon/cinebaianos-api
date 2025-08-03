from di.repository_factory import create_users_repository
from exception.user_already_exists_error import UserAlreadyExistsError
from exception.user_not_found_error import UserNotFoundError
from werkzeug.security import generate_password_hash, check_password_hash

def create_user(conn_provider, discord_id, name, tab, column, email, password):
    user_repo = create_users_repository(conn_provider)
    existing_user = user_repo.get_user(discord_id)

    if existing_user:
        raise UserAlreadyExistsError("Usuário já cadastrado.")

    hashed_password = generate_password_hash(password)

    user = user_repo.register_user(discord_id, name, tab, column, email, hashed_password)

    return user

def get_user(conn_provider, discord_id):
    user_repo = create_users_repository(conn_provider)
    user = user_repo.get_user(discord_id)

    if not user:
        raise UserNotFoundError("Usuário não encontrado")

    return user

def get_user_by_email(conn_provider, email):
    user_repo = create_users_repository(conn_provider)
    user = user_repo.get_user_by_email(email)

    if not user:
        raise UserNotFoundError("Usuário não encontrado")

    return user

def get_all(conn_provider):
    user_repo = create_users_repository(conn_provider)
    users = user_repo.get_all_users()

    return users