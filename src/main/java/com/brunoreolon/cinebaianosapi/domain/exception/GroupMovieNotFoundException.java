package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class GroupMovieNotFoundException extends EntityNotFoundException {

    public GroupMovieNotFoundException(String message, Object[] args) {
        super(message, args, ApiErrorCode.MOVIE_NOT_FOUND);
    }
}