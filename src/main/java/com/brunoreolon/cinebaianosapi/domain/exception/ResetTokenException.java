package com.brunoreolon.cinebaianosapi.domain.exception;

import org.springframework.http.HttpStatus;

public class ResetTokenException extends BusinessException{

    private static final String DEFAULT_TITLE = "Token inválido";
    private static final String DEFAULT_MESSAGE =
            "O token de redefinição de senha é inválido, expirado ou já foi utilizado.";

    public ResetTokenException() {
        super(DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST, DEFAULT_TITLE);
    }

}
