package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class EntityAlreadyRegisteredException extends BusinessException {

    public static final String ENTITY_ALREADY_REGISTERED_TITLE = "entity.already.registered.title";

    public EntityAlreadyRegisteredException(String message, ApiErrorCode apiErrorCode) {
        super(ENTITY_ALREADY_REGISTERED_TITLE, message, HttpStatus.BAD_REQUEST, apiErrorCode.asMap());
    }

    public EntityAlreadyRegisteredException(String message, Object[] args, ApiErrorCode apiErrorCode) {
        super(ENTITY_ALREADY_REGISTERED_TITLE, message, args, HttpStatus.BAD_REQUEST, apiErrorCode.asMap());
    }

}
