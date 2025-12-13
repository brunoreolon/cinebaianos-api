package com.brunoreolon.cinebaianosapi.core.security.authorization.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class OwnershipAccessDeniedException extends RuntimeException {

    private final HttpStatus status = HttpStatus.FORBIDDEN;
    private final String title = "Access denied";

    public OwnershipAccessDeniedException(String message) {
        super(message);
    }

}
