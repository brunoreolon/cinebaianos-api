package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class VoteNotFoundException extends EntityNotFoundException {

    public VoteNotFoundException(String message, Object[] args) {
        super(message, args, ApiErrorCode.INVALID_VOTE);
    }

    public VoteNotFoundException(String message, Object[] args, HttpStatus httpStatus, ApiErrorCode apiErrorCode) {
        super(message, args, httpStatus, apiErrorCode);
    }

}