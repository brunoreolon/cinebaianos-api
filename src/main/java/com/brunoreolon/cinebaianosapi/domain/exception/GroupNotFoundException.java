package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class GroupNotFoundException extends EntityNotFoundException {

    public GroupNotFoundException(String message, Object[] args) {
        super(message, args, ApiErrorCode.GROUP_NOT_FOUND);
    }

}