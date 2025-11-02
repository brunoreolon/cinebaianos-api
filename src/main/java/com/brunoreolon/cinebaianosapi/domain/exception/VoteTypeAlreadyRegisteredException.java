package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class VoteTypeAlreadyRegisteredException extends EntityAlreadyRegisteredException {

    public VoteTypeAlreadyRegisteredException(String message) {
        super(message, ApiErrorCode.VOTE_TYPE_ALREADY_REGISTERED);
    }

}
