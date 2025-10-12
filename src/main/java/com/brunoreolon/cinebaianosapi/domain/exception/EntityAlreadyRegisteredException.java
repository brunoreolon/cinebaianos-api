package com.brunoreolon.cinebaianosapi.domain.exception;

import org.springframework.http.HttpStatus;

public class EntityAlreadyRegisteredException extends BusinessException {

    public static final String ENTITY_ALREADY_REGISTERED_TITLE = "Entity already registered";

    public EntityAlreadyRegisteredException(String message) {
        super(message, HttpStatus.BAD_REQUEST, ENTITY_ALREADY_REGISTERED_TITLE);
    }

}
