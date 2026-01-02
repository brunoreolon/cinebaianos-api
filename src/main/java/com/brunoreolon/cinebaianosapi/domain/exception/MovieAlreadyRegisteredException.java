package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class MovieAlreadyRegisteredException extends EntityAlreadyRegisteredException {

    public MovieAlreadyRegisteredException(String message, Object[] args) {
        super(message, args, ApiErrorCode.MOVIE_ALREADY_REGISTERED);
    }

}