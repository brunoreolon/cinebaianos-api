package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class InvalidRefreshTokenException extends InvalidOrExperidRefreshTokenException {

    public InvalidRefreshTokenException(String message) {
        super(message, ApiErrorCode.INVALID_REFRESH_TOKEN);
    }

}
