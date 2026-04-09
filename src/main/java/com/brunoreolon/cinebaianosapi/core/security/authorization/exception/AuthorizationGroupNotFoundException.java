package com.brunoreolon.cinebaianosapi.core.security.authorization.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class AuthorizationGroupNotFoundException extends RuntimeException {

    public static final String MESSAGE_KEY = "auth.group.not.found.message";

    private final String titleKey = "entity.not.found.title";
    private final HttpStatus status = HttpStatus.NOT_FOUND;
    private final String messageKey;
    private final Object[] args;
    private final Map<String, Object> errorCode = ApiErrorCode.GROUP_NOT_FOUND.asMap();

    public AuthorizationGroupNotFoundException(Object[] args) {
        super(MESSAGE_KEY);
        this.messageKey = MESSAGE_KEY;
        this.args = args;
    }
}