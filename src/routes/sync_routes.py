from flask import Blueprint, jsonify, current_app
from services.sync_service import sincronizar_filmes_e_votos

sync_bp = Blueprint("sync", __name__)

@sync_bp.route("/sincronizar", methods=["POST"])
def sincronizar():
    conn_provider = current_app.config["CONN_PROVIDER"]

    total_filmes, total_votos, elapsed = sincronizar_filmes_e_votos(conn_provider)

    return jsonify({
        "mensagem": "Sincronização concluída com sucesso",
        "total_filmes": total_filmes,
        "total_votos": total_votos,
        "tempo_execucao_segundos": round(elapsed, 2)
    }), 200