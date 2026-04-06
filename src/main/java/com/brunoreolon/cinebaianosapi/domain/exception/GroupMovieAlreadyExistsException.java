package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class GroupMovieAlreadyExistsException extends BusinessException {

    public static final String GROUP_MOVIE_ALREADY_EXISTS_TITLE = "entity.already.registered.title";

    public GroupMovieAlreadyExistsException(String message, Object[] args) {
        super(
                GROUP_MOVIE_ALREADY_EXISTS_TITLE,
                message,
                args,
                org.springframework.http.HttpStatus.CONFLICT,
                ApiErrorCode.GROUP_CONFLICT.asMap()
        );
    }
}