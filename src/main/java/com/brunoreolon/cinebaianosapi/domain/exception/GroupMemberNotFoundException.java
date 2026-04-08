package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class GroupMemberNotFoundException extends EntityNotFoundException {

    public GroupMemberNotFoundException(String message, Object[] args) {
        super(message, args, ApiErrorCode.GROUP_MEMBER_NOT_FOUND);
    }
}