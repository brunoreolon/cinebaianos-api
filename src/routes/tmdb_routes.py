from flask import Blueprint, request, jsonify
from services.tmdb_service import fetch_movie_details

tmdb_bp = Blueprint("tmdb", __name__)

@tmdb_bp.route("/movie-details", methods=["POST"])
def enriquecer_filme():
    data = request.get_json()
    title = data.get("title")
    year = data.get("year")

    if not title:
        return jsonify({"erro": "Campo 'title' é obrigatórios"}), 400

    filme = fetch_movie_details(title, year)
    if not filme:
        return jsonify({"erro": "Filme não encontrado"}), 404

    return jsonify(filme.to_dict()), 200