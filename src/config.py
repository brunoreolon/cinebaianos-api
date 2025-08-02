import os

from dotenv import load_dotenv

class Config:
    load_dotenv()
    TMDB_API_KEY = os.getenv("TMDB_API_KEY")
    JWT_SECRET = os.getenv("JWT_SECRET")
    BOT_API_TOKEN = os.getenv("BOT_API_TOKEN")
    BASE_URL = "https://api.themoviedb.org/3"