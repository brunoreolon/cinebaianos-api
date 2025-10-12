package com.brunoreolon.cinebaianosapi.domain.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends BusinessException {

    public static final String ENTITY_NOT_FOUND_TITLE = "Entity Not Found";
    
    public EntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ENTITY_NOT_FOUND_TITLE);
    }

}
