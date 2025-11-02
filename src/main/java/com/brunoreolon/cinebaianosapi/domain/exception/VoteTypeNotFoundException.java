package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class VoteTypeNotFoundException extends EntityNotFoundException {

    public VoteTypeNotFoundException(String message) {
        super(message, ApiErrorCode.VOTE_TYPE_NOT_FOUND);
    }

}
