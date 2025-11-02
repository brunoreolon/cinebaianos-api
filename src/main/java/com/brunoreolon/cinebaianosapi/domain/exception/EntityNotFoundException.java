package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class EntityNotFoundException extends BusinessException {

    public static final String ENTITY_NOT_FOUND_TITLE = "Entity Not Found";
    
    public EntityNotFoundException(String message, ApiErrorCode apiErrorCode) {
        super(message, HttpStatus.NOT_FOUND, ENTITY_NOT_FOUND_TITLE, Map.of("errorCode", apiErrorCode.getCode()));
    }

}
