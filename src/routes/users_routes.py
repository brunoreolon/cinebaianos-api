from flask import Blueprint, request, jsonify, current_app

from services.users_service import create_user, get_user, get_all

users_bp = Blueprint("users", __name__)

@users_bp.route("/users", methods=["POST"])
def register():
    conn_provider = current_app.config["CONN_PROVIDER"]
    user_data = request.get_json()

    required_fields = ["discord_id", "name", "tab", "column"]
    missing_fields = [f for f in required_fields if not user_data.get(f)]

    if missing_fields:
        return jsonify({"erro": f"Campos obrigatórios ausentes: {', '.join(missing_fields)}"}), 400

    try:
        user = create_user(
            conn_provider,
            user_data["discord_id"],
            user_data["name"],
            user_data["tab"],
            user_data["column"]
        )
        return jsonify(user.to_dict()), 201
    except ValueError as e:
        if str(e) == "Usuário já cadastrado.":
            return jsonify({"erro": str(e)}), 409
    return jsonify({"erro": str(e)}), 400

@users_bp.route("/users/<string:discord_id>", methods=["GET"])
def get_user_discord_id(discord_id):
    conn_provider = current_app.config["CONN_PROVIDER"]

    try:
        user = get_user(conn_provider, discord_id)
        return jsonify(user.to_dict()), 200
    except ValueError as e:
        return jsonify({"erro": str(e)}), 404

@users_bp.route("/users", methods=["GET"])
def get_users():
    conn_provider = current_app.config["CONN_PROVIDER"]
    users = get_all(conn_provider)
    users_dict = [user.to_dict() for user in users]

    return jsonify(users_dict), 200