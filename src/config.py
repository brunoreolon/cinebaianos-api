import os

from dotenv import load_dotenv

load_dotenv()

class Config:
    TMDB_API_KEY = os.getenv("TMDB_API_KEY")
    JWT_SECRET = os.getenv("JWT_SECRET")
    BOT_API_TOKEN = os.getenv("BOT_API_TOKEN")
    ORIGINS = os.getenv("FRONTEND_ORIGINS").split(",")
    BASE_URL = "https://api.themoviedb.org/3"
    PORT = int(os.getenv("PORT"))