import logging
import time

from services.tmdb_service import fetch_movie_details
from src.di.repository_factory import create_movies_repository, create_votes_repository
from src.di.maintenance_factory import create_maintenance_repository
from src.infra.sheets.sheets import read_all_movies, read_votes_from_spreadsheet

def synchronize_movies_with_spreadsheet(conn_provider):
    movie_repo = create_movies_repository(conn_provider)
    maintenance_repo = create_maintenance_repository(conn_provider)

    logging.info("🔄 Sincronizando filmes com a planilha...\n")

    logging.info("Limpando o banco...")
    maintenance_repo.clear_movie_bank()

    logging.info("Lendo filmes da planiha...\n")
    movies_spreadsheet = read_all_movies(conn_provider)
    total_movies = 0

    # 3. Adicionar no banco com dados enriquecidos
    for movie in movies_spreadsheet:
        title = movie['titulo']
        year = movie['ano']
        id_responsavel = movie['id_responsavel']
        id_linha = movie['id_linha']

        logging.info(f"🔍 Buscando: {title}")
        details = fetch_movie_details(title, year)
        logging.info(f"Detalhes encontrados:\n{details}")

        if details:
            movie_repo.add_movie(
                tmdb_id=details.id,
                titulo=details.title,
                id_responsavel=id_responsavel,
                linha_planilha=id_linha,
                year=details.year,
                genero=details.genres[0]["name"] if details.genres else "Indefinido"
            )

            logging.info(f"✅ {details.title} ({details.year}) adicionado.\n")
            total_movies += 1
        else:
            logging.info(f"⚠️ Detalhes não encontrados: {details} ({details})")

    return total_movies


def synchronize_votes_with_spreadsheet(conn_provider):
    movie_repo = create_movies_repository(conn_provider)
    vote_repo = create_votes_repository(conn_provider)

    logging.info("🔄 Sincronizando votes com a planilha...\n")

    # 2. Carregar os votes da planilha
    votes = read_votes_from_spreadsheet(conn_provider)  # Cada item deve conter: row_id, voter_id, responsible_id, vote
    logging.info(f"📌 Total de votes encontrados: {len(votes)}\n")
    total_votes = 0

    for vote in votes:
        responsible_id = vote["responsible_id"]
        responsible_name = vote["responsible_name"]
        voter_id = vote["voter_id"]
        voter_name = vote["voter_name"]
        row_id = vote["row_id"]
        tab = vote["tab"]
        vote_value = vote["vote"]

        logging.info(f"🔍 Processando vote: Aba={tab}, linha={row_id}, votante={voter_name}, responsavel={responsible_name}, vote={vote_value}")

        movie_info = movie_repo.find_movie_by_row_and_user(responsible_id, row_id)
        if not movie_info:
            logging.info(f"❌ Filme não encontrado para responsavel={responsible_name}, linha={row_id}")
            continue

        movie_id, movie_title = movie_info
        vote_repo.register_vote(movie_id, responsible_id, voter_id, vote_value)
        logging.info(f"🗳️ Voto registrado: {voter_name} votou '{vote_value}' no filme '{movie_title}' (Aba={tab}, Responsável={responsible_name}, linha {row_id})\n")
        total_votes += 1

    logging.info("✅ Sincronização de votes concluída.")
    return total_votes


def sync_movies_and_votes(conn_provider):
    start_time = time.time()

    total_movies = synchronize_movies_with_spreadsheet(conn_provider)
    total_votes = synchronize_votes_with_spreadsheet(conn_provider)

    elapsed = time.time() - start_time

    return total_movies, total_votes, elapsed