package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends BusinessException {

    public static final String ENTITY_NOT_FOUND_TITLE = "entity.not.found.title";

    public EntityNotFoundException(String message, ApiErrorCode apiErrorCode) {
        super(ENTITY_NOT_FOUND_TITLE, message, HttpStatus.NOT_FOUND, apiErrorCode.asMap());
    }

    public EntityNotFoundException(String message, Object[] args, ApiErrorCode apiErrorCode) {
        super(ENTITY_NOT_FOUND_TITLE, message, args, HttpStatus.NOT_FOUND, apiErrorCode.asMap());
    }

}
