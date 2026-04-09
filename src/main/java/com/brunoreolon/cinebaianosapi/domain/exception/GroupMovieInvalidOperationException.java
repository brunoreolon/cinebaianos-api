package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class GroupMovieInvalidOperationException extends BusinessException {

    public static final String GROUP_MOVIE_INVALID_OPERATION_TITLE = "action.not.allowed.title";

    public GroupMovieInvalidOperationException(String message) {
        super(
                GROUP_MOVIE_INVALID_OPERATION_TITLE,
                message,
                HttpStatus.UNPROCESSABLE_ENTITY,
                ApiErrorCode.GROUP_INVALID_OPERATION.asMap()
        );
    }

    public GroupMovieInvalidOperationException(String message, Object[] args) {
        super(
                GROUP_MOVIE_INVALID_OPERATION_TITLE,
                message,
                args,
                HttpStatus.UNPROCESSABLE_ENTITY,
                ApiErrorCode.GROUP_INVALID_OPERATION.asMap()
        );
    }
}

