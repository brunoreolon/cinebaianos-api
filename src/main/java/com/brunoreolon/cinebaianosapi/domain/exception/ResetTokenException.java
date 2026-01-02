package com.brunoreolon.cinebaianosapi.domain.exception;

import org.springframework.http.HttpStatus;

public class ResetTokenException extends BusinessException{

    private static final String DEFAULT_TITLE = "invalid.reset.token.title";
    private static final String DEFAULT_MESSAGE = "invalid.reset.token.message";

    public ResetTokenException() {
        super(DEFAULT_TITLE, DEFAULT_MESSAGE, HttpStatus.BAD_REQUEST);
    }

}