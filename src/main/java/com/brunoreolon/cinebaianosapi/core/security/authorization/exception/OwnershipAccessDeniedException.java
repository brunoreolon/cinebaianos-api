package com.brunoreolon.cinebaianosapi.core.security.authorization.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class OwnershipAccessDeniedException extends RuntimeException {

    private final HttpStatus status = HttpStatus.FORBIDDEN;
    private final String title = "Access denied";
    private final Map<String, Object> errorCode = ApiErrorCode.USER_NOT_AUTHORIZED.asMap();

    public OwnershipAccessDeniedException(String message) {
        super(message);
    }

}
