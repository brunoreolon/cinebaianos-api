package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class VoteNotFoundException extends EntityNotFoundException {

    public VoteNotFoundException(String message) {
        super(message, ApiErrorCode.INVALID_VOTE);
    }

}
