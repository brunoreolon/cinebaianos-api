package com.brunoreolon.cinebaianosapi.domain.exception;

import org.springframework.http.HttpStatus;

public class EntityInUseException extends BusinessException {

    public EntityInUseException(String message) {
        super(message, HttpStatus.CONFLICT, "Entity in use");
    }

}
