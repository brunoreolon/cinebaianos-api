package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class MovieNotFoundException extends EntityNotFoundException {

    public MovieNotFoundException(String message) {
        super(message, ApiErrorCode.MOVIE_NOT_FOUND);
    }

}
