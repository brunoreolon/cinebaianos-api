from flask import Blueprint, jsonify, current_app
from services.sync_service import sync_movies_and_votes

sync_bp = Blueprint("sync", __name__)

@sync_bp.route("/sincronizar", methods=["POST"])
def sync():
    conn_provider = current_app.config["CONN_PROVIDER"]

    total_movies, total_votes, elapsed = sync_movies_and_votes(conn_provider)

    return jsonify({
        "message": "Sincronização concluída com sucesso",
        "total_movie": total_movies,
        "total_votes": total_votes,
        "execution_time_seconds": round(elapsed, 2)
    }), 200