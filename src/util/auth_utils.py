import jwt
from flask import current_app

def decode_jwt(token, verify_exp=True):
    options = {"verify_exp": verify_exp}
    return jwt.decode(token, current_app.config["JWT_SECRET"], algorithms=["HS256"], options=options)
