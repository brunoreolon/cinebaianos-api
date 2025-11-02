package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class UserAlreadyRegisteredException extends EntityAlreadyRegisteredException {

    public UserAlreadyRegisteredException(String message) {
        super(message, ApiErrorCode.MOVIE_ALREADY_REGISTERED);
    }

}
