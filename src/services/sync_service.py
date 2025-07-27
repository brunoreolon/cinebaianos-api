import logging
import time

from flask import Blueprint, request, jsonify
from services.tmdb_service import buscar_detalhes_filme
from src.di.repository_factory import criar_filmes_repository, criar_votos_repository
from src.di.maintenance_factory import criar_maintenance_repository
from src.infra.sheets.sheets import ler_todos_os_filmes, ler_votos_da_planilha

sync_bp = Blueprint("sync", __name__)

def sincronizar_filmes_com_planilha(conn_provider):
    filme_repo = criar_filmes_repository(conn_provider)
    maintenance_repo = criar_maintenance_repository(conn_provider)

    logging.info("🔄 Sincronizando filmes com a planilha...\n")

    logging.info("Limpando o banco...")
    maintenance_repo.limpar_banco_filmes()

    logging.info("Lendo filmes da planiha...\n")
    filmes_planilha = ler_todos_os_filmes(conn_provider)
    total_filmes = 0

    # 3. Adicionar no banco com dados enriquecidos
    for filme in filmes_planilha:
        titulo = filme['titulo']
        ano = filme['ano']
        id_responsavel = filme['id_responsavel']
        id_linha = filme['id_linha']

        logging.info(f"🔍 Buscando: {titulo}")
        detalhes = buscar_detalhes_filme(titulo, ano)
        logging.info(f"Detalhes encontrados:\n{detalhes}")

        if detalhes:
            filme_repo.adicionar_filme(
                tmdb_id=detalhes.id,
                titulo=detalhes.title,
                id_responsavel=id_responsavel,
                linha_planilha=id_linha,
                ano=detalhes.ano,
                genero=detalhes.genres[0]["name"] if detalhes.genres else "Indefinido"
            )

            logging.info(f"✅ {detalhes.title} ({detalhes.ano}) adicionado.\n")
            total_filmes += 1
        else:
            logging.info(f"⚠️ Detalhes não encontrados: {detalhes} ({detalhes})")

    return total_filmes


def sincronizar_votos_com_planilha(conn_provider):
    filme_repo = criar_filmes_repository(conn_provider)
    voto_repo = criar_votos_repository(conn_provider)

    logging.info("🔄 Sincronizando votos com a planilha...\n")

    # 2. Carregar os votos da planilha
    votos = ler_votos_da_planilha(conn_provider)  # Cada item deve conter: id_linha, id_votante, id_responsavel, voto
    logging.info(f"📌 Total de votos encontrados: {len(votos)}\n")
    total_votos = 0

    for voto in votos:
        id_responsavel = voto["id_responsavel"]
        nome_responsavel = voto["nome_responsavel"]
        id_votante = voto["id_votante"]
        nome_votante = voto["nome_votante"]
        id_linha = voto["id_linha"]
        aba = voto["aba"]
        valor_voto = voto["voto"]

        logging.info(f"🔍 Processando voto: Aba={aba}, linha={id_linha}, votante={nome_votante}, responsavel={nome_responsavel}, voto={valor_voto}")

        filme_info = filme_repo.buscar_filme_por_linha_e_usuario(id_responsavel, id_linha)
        if not filme_info:
            logging.info(f"❌ Filme não encontrado para responsavel={nome_responsavel}, linha={id_linha}")
            continue

        id_filme, titulo_filme = filme_info
        voto_repo.registrar_voto(id_filme, id_responsavel, id_votante, valor_voto)
        logging.info(f"🗳️ Voto registrado: {nome_votante} votou '{valor_voto}' no filme '{titulo_filme}' (Aba={aba}, Responsável={nome_responsavel}, linha {id_linha})\n")
        total_votos += 1

    logging.info("✅ Sincronização de votos concluída.")
    return total_votos


def sincronizar_filmes_e_votos(conn_provider):
    start_time = time.time()

    total_filmes = sincronizar_filmes_com_planilha(conn_provider)
    total_votos = sincronizar_votos_com_planilha(conn_provider)

    elapsed = time.time() - start_time

    return total_filmes, total_votos, elapsed