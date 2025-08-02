from flask import Flask

from config import Config
from di.connection_factory import get_connection_provider
from di.schemas_factory import create_schemas_repository
from routes.genres_routes import genres_bp
from routes.movies_routes import movies_bp
from routes.rankings_routes import rankings_bp
from routes.sync_routes import sync_bp
from routes.tmdb_routes import tmdb_bp
from routes.users_routes import users_bp
from routes.votes_routes import votes_bp


def create_app():
    app = Flask(__name__)

    conn_provider = get_connection_provider()
    schema_repo = create_schemas_repository(conn_provider)
    schema_repo.create_tables()

    app.config.from_object(Config)
    app.config["CONN_PROVIDER"] = conn_provider

    app.register_blueprint(users_bp, url_prefix="/api")
    app.register_blueprint(movies_bp, url_prefix="/api")
    app.register_blueprint(votes_bp, url_prefix="/api")
    app.register_blueprint(genres_bp, url_prefix="/api")
    app.register_blueprint(rankings_bp, url_prefix="/api")
    app.register_blueprint(tmdb_bp, url_prefix="/api")
    app.register_blueprint(sync_bp, url_prefix="/api")

    for rule in app.url_map.iter_rules():
        print(rule)

    return app

if __name__ == "__main__":
    app = create_app()
    app.run(debug=True)