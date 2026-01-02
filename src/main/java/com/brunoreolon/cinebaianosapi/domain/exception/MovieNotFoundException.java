package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class MovieNotFoundException extends EntityNotFoundException {

    public MovieNotFoundException(String message, Object[] args) {
        super(message, args, ApiErrorCode.MOVIE_NOT_FOUND);
    }

}