import os

from dotenv import load_dotenv

class Config:
    load_dotenv()
    TMDB_API_KEY = os.getenv("TMDB_API_KEY")
    BASE_URL = "https://api.themoviedb.org/3"