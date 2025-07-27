from flask import Flask

from config import Config
from di.connection_factory import get_connection_provider
from di.schemas_factory import get_schemas_repository
from routes.sync_routes import sync_bp
from routes.tmdb_routes import tmdb_bp

def create_app():
    app = Flask(__name__)

    conn_provider = get_connection_provider()
    schema_repo = get_schemas_repository(conn_provider)
    schema_repo.criar_tabelas()

    app.config.from_object(Config)
    app.config["CONN_PROVIDER"] = conn_provider
    app.register_blueprint(tmdb_bp, url_prefix="/api")
    app.register_blueprint(sync_bp, url_prefix="/api")

    return app

if __name__ == "__main__":
    app = create_app()
    app.run(debug=True)