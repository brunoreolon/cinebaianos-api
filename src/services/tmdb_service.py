import requests
from config import Config
from models.filme import Filme

def buscar_detalhes_filme(titulo, ano):
    url = f"{Config.BASE_URL}/search/movie"
    params = {
        "api_key": Config.TMDB_API_KEY,
        "query": titulo,
        "year": ano,
        "language": "pt-BR"
    }

    if ano:
        params["year"] = ano

    response = requests.get(url, params=params)

    if response.status_code != 200 or not response.json().get("results"):
        return None

    filme_id = response.json()["results"][0]["id"]

    # Buscar detalhes
    detalhes_url = f"{Config.BASE_URL}/movie/{filme_id}"
    detalhes_params = {
        "api_key": Config.TMDB_API_KEY,
        "language": "pt-BR"
    }

    detalhes_resp = requests.get(detalhes_url, params=detalhes_params)
    if detalhes_resp.status_code != 200:
        return None

    data = detalhes_resp.json()
    return Filme(
        id=data.get("id"),
        title=data.get("title"),
        genres=data.get("genres"),
        poster_path=data.get("poster_path"),
        ano=data.get("release_date", "").split("-")[0]
    )