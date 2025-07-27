from flask import Blueprint, request, jsonify
from services.tmdb_service import buscar_detalhes_filme

tmdb_bp = Blueprint("tmdb", __name__)

@tmdb_bp.route("/enriquecer-filme", methods=["POST"])
def enriquecer_filme():
    data = request.get_json()
    titulo = data.get("titulo")
    ano = data.get("ano")

    if not titulo:
        return jsonify({"erro": "Campo 'titulo' é obrigatórios"}), 400

    filme = buscar_detalhes_filme(titulo, ano)
    if not filme:
        return jsonify({"erro": "Filme não encontrado"}), 404

    return jsonify(filme.to_dict()), 200