package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class MovieAlreadyRegisteredException extends EntityAlreadyRegisteredException {

    public MovieAlreadyRegisteredException(String message) {
        super(message, ApiErrorCode.MOVIE_ALREADY_REGISTERED);
    }

}
