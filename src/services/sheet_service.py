import os
import json
import gspread
import logging

from dotenv import load_dotenv
from google.oauth2 import service_account
from gspread_formatting import format_cell_range, CellFormat, TextFormat

from exception.column_not_found_error import ColumnNotFoundError
from exception.spread_sheet_error import SpreadsheetError
from src.di.repository_factory import create_users_repository

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

def get_sheet():
    return sheet

def add_movie_to_spreadsheet(title, tab, column, vote=None):
    tab_obj = sheet.worksheet(tab)

    # Obtem todas as células da coluna B (títulos)
    col_b = tab_obj.col_values(2)  # coluna B

    # Acha a próxima row disponível após os filmes já adicionados
    # Começa na row 2 para ignorar o cabeçalho
    row = len(col_b) + 1 if len(col_b) >= 2 else 2

    logging.info(f"Inserindo filme na aba: {tab}, row {row}: {title}")

    # Escreve o título na coluna B
    tab_obj.update_cell(row, 2, title)

    # Aplica a formatação (Arial 11 Negrito)
    fmt = CellFormat(textFormat=TextFormat(fontFamily="Arial", fontSize=11, bold=True))
    format_cell_range(tab_obj, f"B{row}", fmt)

    # Escreve o voto na coluna correta
    header = tab_obj.row_values(4) or []
    clean_columns = [c.strip().upper() for c in header]
    target_column = column.strip().upper()

    if vote and target_column in clean_columns:
        column_index = clean_columns.index(target_column) + 1
        tab_obj.update_cell(row, column_index, vote)
    elif not vote:
        logging.info("📝 Nenhum voto informado. Apenas adicionando o filme.")
    else:
        logging.info(f"⚠️ Coluna '{column}' não encontrada no cabeçalho: {header}")

    return row

def add_vote_to_spreadsheet(tab, row, column, vote):
    try:
        tab_obj = sheet.worksheet(tab)
        header = tab_obj.row_values(4)  # ← linha do cabeçalho
        columns_upper = [c.upper() for c in header]

        if column.upper() in columns_upper:
            column_index = columns_upper.index(column.upper()) + 1
            tab_obj.update_cell(row, column_index, vote)
            logging.info(f"✅ Voto '{vote}' salvo na célula {row}, {column_index} (coluna {column})")
            return True
        else:
            logging.info(f"⚠️ Coluna '{column}' não encontrada no cabeçalho da aba '{tab}'. Cabeçalho: {header}")
            raise ColumnNotFoundError(f"Coluna '{column}' não encontrada na aba '{tab}'")
    except Exception as e:
        logging.info(f"❌ Erro ao escrever voto na planilha: {e}")
        raise SpreadsheetError("Erro inesperado ao salvar voto")

def read_all_movies(conn_provider):
    user_repo = create_users_repository(conn_provider)

    sheett = get_sheet()
    users = user_repo.get_all_users()
    logging.info(f"Usuários encontrados: {users}\n")

    found_movies = []

    for user in users:
        logging.info(f"Processando usuário: {user}")
        discord_id = user.discord_id
        tab = user.tab

        try:
            tab_sheet = sheett.worksheet(tab)
            data = tab_sheet.get_all_values()

            for i, row in enumerate(data[4:], start=5): # Pula o cabeçalho, começa na row 2
                logging.info(f"Linha: {row}")

                raw_title= row[1].strip()

                if not raw_title:
                    continue

                name_with_year = raw_title.strip()

                if "(" in name_with_year and ")" in name_with_year:
                    title = name_with_year[:name_with_year.rfind("(")].strip()
                    year = name_with_year[name_with_year.rfind("(") + 1:name_with_year.rfind(")")].strip()
                else:
                    title = name_with_year
                    year = None

                found_movies.append({
                    "title": title,
                    "responsible_id": discord_id,
                    "year": year,
                    "spreadsheet_row": i
                })

        except Exception as e:
            logging.info(f"Erro ao processar tab {tab}: {e}")
            raise SpreadsheetError(f"Erro ao processar tab {tab}: {e}")

    return found_movies

def read_votes_from_spreadsheet(conn_provider):
    user_repo = create_users_repository(conn_provider)

    sheett = get_sheet()
    users = user_repo.get_all_users()
    votes = []

    # Mapa COLUNA -> {id, name}
    column_to_user_map = {
        user.column.upper(): {
            "id": user.discord_id,
            "name": user.name
        }
        for user in users
    }

    # Mapa ID → name do usuário (para descobrir name do responsável pela tab)
    id_to_name_map = {
        user.discord_id: user.name
        for user in users
    }

    logging.info(f"Usuários mapeados: {column_to_user_map}\n")

    tabs = sheett.worksheets()
    for tab in tabs:
        sheet_name = tab.title.strip()

        if sheet_name.upper() == "DASHBOARD":
            logging.info(f"⏭️ Ignorando tab {sheet_name}\n")
            continue

        logging.info(f"📄 Processando tab: {sheet_name}")
        dados = tab.get_all_values()

        if len(dados) < 4:
            logging.info(f"⚠️ Cabeçalho ausente ou muito curto na tab {sheet_name}")
            continue

        header = dados[3]

        # Descobrir dono da tab
        responsible_id = None
        for user in users:
            if user.tab.strip().upper() == sheet_name.upper():
                responsible_id = user.discord_id
                break

        if not responsible_id:
            logging.info(f"⚠️ Usuário responsável pela tab '{sheet_name}' não encontrado. Pulando.")
            continue

        responsible_name = id_to_name_map.get(responsible_id, "Desconhecido")

        for col_idx in range(2, len(header)):
            column_name = header[col_idx].strip().upper()
            logging.info(f"🔍 Verificando column: {column_name}")

            user_voter = column_to_user_map.get(column_name)

            if not user_voter:
                logging.info(f"⚠️ Coluna '{column_name}' ignorada (usuário não cadastrado).")
                continue

            voter_id = user_voter["id"]
            voter_name = user_voter["name"]
            logging.info(f"ID do votante: {voter_id} ({voter_name})\n")

            for i, row in enumerate(dados[4:], start=5):
                if len(row) <= col_idx:
                    continue

                title = row[1].strip() if len(row) > 1 else ""
                vote = row[col_idx].strip().upper()

                if title and vote in ["DA HORA", "LIXO", "NÃO ASSISTI"]:
                    logging.info(f"✅ Voto válido: '{vote}' por {voter_name} no filme '{title}' (row {i}) da tab '{sheet_name}' (Filme de: {responsible_name})")
                    votes.append({
                        "responsible_id": responsible_id,
                        "responsible_name": responsible_name,
                        "voter_id": voter_id,
                        "voter_name": voter_name,
                        "row_id": i,
                        "tab": sheet_name,
                        "vote": vote
                    })
                    logging.info(f"Voto adicionado: {votes[-1]}\n")

    return votes