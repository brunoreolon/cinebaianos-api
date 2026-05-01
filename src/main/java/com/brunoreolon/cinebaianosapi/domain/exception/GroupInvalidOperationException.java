package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class GroupInvalidOperationException extends BusinessException {

    public static final String GROUP_INVALID_OPERATION_TITLE = "action.not.allowed.title";

    public GroupInvalidOperationException(String message) {
        super(
                GROUP_INVALID_OPERATION_TITLE,
                message,
                HttpStatus.UNPROCESSABLE_ENTITY,
                ApiErrorCode.GROUP_INVALID_OPERATION.asMap()
        );
    }

    public GroupInvalidOperationException(String message, Object[] args) {
        super(
                GROUP_INVALID_OPERATION_TITLE,
                message,
                args,
                HttpStatus.UNPROCESSABLE_ENTITY,
                ApiErrorCode.GROUP_INVALID_OPERATION.asMap()
        );
    }
}