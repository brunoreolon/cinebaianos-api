package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidRefreshTokenException extends InvalidOrExperidRefreshTokenException {

    public InvalidRefreshTokenException(String message) {
        super(message, ApiErrorCode.INVALID_REFRESH_TOKEN);
    }

}
