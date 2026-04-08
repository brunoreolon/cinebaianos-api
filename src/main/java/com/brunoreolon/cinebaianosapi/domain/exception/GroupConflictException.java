package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class GroupConflictException extends BusinessException {

    public static final String GROUP_CONFLICT_TITLE = "entity.already.registered.title";

    public GroupConflictException(String message, Object[] args) {
        super(GROUP_CONFLICT_TITLE, message, args, HttpStatus.CONFLICT, ApiErrorCode.GROUP_CONFLICT.asMap());
    }
}