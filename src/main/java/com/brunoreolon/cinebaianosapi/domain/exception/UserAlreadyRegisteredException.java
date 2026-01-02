package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class UserAlreadyRegisteredException extends EntityAlreadyRegisteredException {

    public UserAlreadyRegisteredException(String message, Object[] args) {
        super(message, args, ApiErrorCode.USER_ALREADY_REGISTERED);
    }

}