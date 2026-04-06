package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class GroupMemberAlreadyExistsException extends BusinessException {

    public static final String GROUP_MEMBER_ALREADY_EXISTS_TITLE = "entity.already.registered.title";

    public GroupMemberAlreadyExistsException(String message, Object[] args) {
        super(
                GROUP_MEMBER_ALREADY_EXISTS_TITLE,
                message,
                args,
                HttpStatus.CONFLICT,
                ApiErrorCode.GROUP_MEMBER_ALREADY_EXISTS.asMap()
        );
    }
}

