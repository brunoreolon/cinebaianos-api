package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class EntityInUseException extends BusinessException {

    public static final String ENTITY_IN_USE_TITLE = "entity.in.use.title";

    public EntityInUseException(String message, ApiErrorCode apiErrorCode) {
        super(ENTITY_IN_USE_TITLE, message, HttpStatus.CONFLICT, apiErrorCode.asMap());
    }

    public EntityInUseException(String message, Object[] args, ApiErrorCode apiErrorCode) {
        super(ENTITY_IN_USE_TITLE, message, args, HttpStatus.CONFLICT, apiErrorCode.asMap());
    }

}