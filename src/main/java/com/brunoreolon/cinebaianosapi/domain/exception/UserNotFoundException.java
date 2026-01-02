package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class UserNotFoundException extends EntityNotFoundException {

    public UserNotFoundException(String message, Object[] args) {
        super(message, args, ApiErrorCode.USER_NOT_FOUND);
    }

}