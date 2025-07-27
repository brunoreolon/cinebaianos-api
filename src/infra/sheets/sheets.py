import os
import json
import gspread
import logging

from dotenv import load_dotenv
from google.oauth2 import service_account
from gspread_formatting import format_cell_range, CellFormat, TextFormat

from src.di.repository_factory import criar_usuarios_repository

load_dotenv()

SHEET_ID = os.getenv("SHEET_ID")
SERVICE_ACCOUNT_FILE = os.getenv("GOOGLE_SHEETS_CREDENTIALS")
logging.info(f"GOOGLE_SHEETS_CREDENTIALS: {SERVICE_ACCOUNT_FILE is not None}")

SERVICE_ACCOUNT_JSON = os.getenv("GOOGLE_SHEETS_CREDENTIALS_JSON")
logging.info(f"GOOGLE_SHEETS_CREDENTIALS_JSON: {SERVICE_ACCOUNT_JSON is not None}")


if SERVICE_ACCOUNT_JSON:
    creds_dict = json.loads(SERVICE_ACCOUNT_JSON)
    scopes = ["https://www.googleapis.com/auth/spreadsheets"]
    credentials = service_account.Credentials.from_service_account_info(creds_dict, scopes=scopes)
    gc = gspread.authorize(credentials)
else:
    gc = gspread.service_account(filename=SERVICE_ACCOUNT_FILE)

sheet = gc.open_by_key(SHEET_ID)

def get_planilha():
    return sheet

def adicionar_filme_na_planilha(titulo, aba, coluna, voto=None):
    aba_obj = sheet.worksheet(aba)

    # Obtem todas as células da coluna B (títulos)
    col_b = aba_obj.col_values(2)  # coluna B

    # Acha a próxima linha disponível após os filmes já adicionados
    # Começa na linha 2 para ignorar o cabeçalho
    linha = len(col_b) + 1 if len(col_b) >= 2 else 2

    logging.info(f"Inserindo filme na aba: {aba}, linha {linha}: {titulo}")

    # Escreve o título na coluna B
    aba_obj.update_cell(linha, 2, titulo)

    # Aplica a formatação (Arial 11 Negrito)
    fmt = CellFormat(textFormat=TextFormat(fontFamily="Arial", fontSize=11, bold=True))
    format_cell_range(aba_obj, f"B{linha}", fmt)

    # Escreve o voto na coluna correta
    cabecalho = aba_obj.row_values(4)
    colunas_limpas = [c.strip().upper() for c in cabecalho]
    coluna_alvo = coluna.strip().upper()

    if voto and coluna_alvo in colunas_limpas:
        index_coluna = colunas_limpas.index(coluna_alvo) + 1
        aba_obj.update_cell(linha, index_coluna, voto)
    elif not voto:
        logging.info("📝 Nenhum voto informado. Apenas adicionando o filme.")
    else:
        logging.info(f"⚠️ Coluna '{coluna}' não encontrada no cabeçalho: {cabecalho}")

    return linha

def escrever_voto_na_planilha(aba, linha, coluna, voto):
    try:
        aba_obj = sheet.worksheet(aba)
        cabecalho = aba_obj.row_values(4)  # ← linha do cabeçalho
        colunas_upper = [c.upper() for c in cabecalho]

        if coluna.upper() in colunas_upper:
            index_coluna = colunas_upper.index(coluna.upper()) + 1
            aba_obj.update_cell(linha, index_coluna, voto)
            logging.info(f"✅ Voto '{voto}' salvo na célula {linha}, {index_coluna} (coluna {coluna})")
            return True
        else:
            logging.info(f"⚠️ Coluna '{coluna}' não encontrada no cabeçalho da aba '{aba}'. Cabeçalho: {cabecalho}")
            return False
    except Exception as e:
        logging.info(f"❌ Erro ao escrever voto na planilha: {e}")
        return False

def ler_todos_os_filmes(conn_provider):
    usuario_repo = criar_usuarios_repository(conn_provider)

    planilha = get_planilha()
    usuarios = usuario_repo.buscar_todos_os_usuarios()  # Deve retornar lista de tuplas: (id, nome, aba, coluna)
    logging.info(f"Usuários encontrados: {usuarios}\n")

    filmes_encontrados = []

    for usuario in usuarios:
        logging.info(f"Processando usuário: {usuario}")
        discord_id, nome, aba, coluna = usuario

        try:
            aba_sheet = planilha.worksheet(aba)
            dados = aba_sheet.get_all_values()

            for i, linha in enumerate(dados[4:], start=5): # Pula o cabeçalho, começa na linha 2
                logging.info(f"Linha: {linha}")

                titulo_raw = linha[1].strip()

                if not titulo_raw:
                    continue

                nome_com_ano = titulo_raw.strip()

                if "(" in nome_com_ano and ")" in nome_com_ano:
                    titulo = nome_com_ano[:nome_com_ano.rfind("(")].strip()
                    ano = nome_com_ano[nome_com_ano.rfind("(") + 1:nome_com_ano.rfind(")")].strip()
                else:
                    titulo = nome_com_ano
                    ano = None

                filmes_encontrados.append({
                    "titulo": titulo,
                    "id_responsavel": discord_id,
                    "ano": ano,
                    "id_linha": i
                })

        except Exception as e:
            logging.info(f"Erro ao processar aba {aba}: {e}")

    return filmes_encontrados

def ler_votos_da_planilha(conn_provider):
    usuario_repo = criar_usuarios_repository(conn_provider)

    planilha = get_planilha()
    usuarios = usuario_repo.buscar_todos_os_usuarios()
    votos = []

    # Mapa COLUNA -> {id, nome}
    mapa_coluna_para_usuario = {
        coluna.upper(): {
            "id": discord_id,
            "nome": nome
        }
        for discord_id, nome, _, coluna in usuarios
    }

    # Mapa ID → nome do usuário (para descobrir nome do responsável pela aba)
    mapa_id_para_nome = {
        discord_id: nome
        for discord_id, nome, _, _ in usuarios
    }

    logging.info(f"Usuários mapeados: {mapa_coluna_para_usuario}\n")

    abas = planilha.worksheets()
    for aba in abas:
        nome_aba = aba.title.strip()

        if nome_aba.upper() == "DASHBOARD":
            logging.info(f"⏭️ Ignorando aba {nome_aba}\n")
            continue

        logging.info(f"📄 Processando aba: {nome_aba}")
        dados = aba.get_all_values()

        if len(dados) < 4:
            logging.info(f"⚠️ Cabeçalho ausente ou muito curto na aba {nome_aba}")
            continue

        cabecalho = dados[3]

        # Descobrir dono da aba
        id_responsavel = None
        for usuario in usuarios:
            if usuario[2].strip().upper() == nome_aba.upper():
                id_responsavel = usuario[0]
                break

        if not id_responsavel:
            logging.info(f"⚠️ Usuário responsável pela aba '{nome_aba}' não encontrado. Pulando.")
            continue

        nome_responsavel = mapa_id_para_nome.get(id_responsavel, "Desconhecido")

        for col_idx in range(2, len(cabecalho)):
            nome_coluna = cabecalho[col_idx].strip().upper()
            logging.info(f"🔍 Verificando coluna: {nome_coluna}")

            usuario_votante = mapa_coluna_para_usuario.get(nome_coluna)

            if not usuario_votante:
                logging.info(f"⚠️ Coluna '{nome_coluna}' ignorada (usuário não cadastrado).")
                continue

            id_votante = usuario_votante["id"]
            nome_votante = usuario_votante["nome"]
            logging.info(f"ID do votante: {id_votante} ({nome_votante})\n")

            for i, linha in enumerate(dados[4:], start=5):
                if len(linha) <= col_idx:
                    continue

                titulo = linha[1].strip() if len(linha) > 1 else ""
                voto = linha[col_idx].strip().upper()

                if titulo and voto in ["DA HORA", "LIXO", "NÃO ASSISTI"]:
                    logging.info(f"✅ Voto válido: '{voto}' por {nome_votante} no filme '{titulo}' (linha {i}) da aba '{nome_aba}' (Filme de: {nome_responsavel})")
                    votos.append({
                        "id_responsavel": id_responsavel,
                        "nome_responsavel": nome_responsavel,
                        "id_votante": id_votante,
                        "nome_votante": nome_votante,
                        "id_linha": i,
                        "aba": nome_aba,
                        "voto": voto
                    })
                    logging.info(f"Voto adicionado: {votos[-1]}\n")

    return votos


