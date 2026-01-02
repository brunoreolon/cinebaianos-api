package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class VoteAlreadyRegisteredException extends EntityAlreadyRegisteredException {

    public VoteAlreadyRegisteredException(String message, Object[] args) {
        super(message, args, ApiErrorCode.VOTE_ALREADY_REGISTERED);
    }

}