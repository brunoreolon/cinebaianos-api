import requests
from config import Config
from models.movie_detail import MovieDetail


def fetch_movie_details(title, year):
    url = f"{Config.BASE_URL}/search/movie"
    params = {
        "api_key": Config.TMDB_API_KEY,
        "query": title,
        "year": year,
        "language": "pt-BR"
    }

    if year:
        params["year"] = year

    response = requests.get(url, params=params)

    if response.status_code != 200 or not response.json().get("results"):
        return None

    movie_id = response.json()["results"][0]["id"]

    details_url = f"{Config.BASE_URL}/movie/{movie_id}"
    details_params = {
        "api_key": Config.TMDB_API_KEY,
        "language": "pt-BR"
    }

    details_resp = requests.get(details_url, params=details_params)
    if details_resp.status_code != 200:
        return None

    data = details_resp.json()

    genres = data.get("genres", [])
    first_genre = genres[0]["name"] if genres else "Indefinido"
    year_str = data.get("release_date", "").split("-")[0]

    return MovieDetail(
        id=data.get("id"),
        title=data.get("title"),
        genre=first_genre,
        year=year_str
    )