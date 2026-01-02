package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

public class ExpiredRefreshTokenException extends InvalidOrExperidRefreshTokenException {

    public ExpiredRefreshTokenException(String message) {
        super(message, ApiErrorCode.EXPIRED_REFRESH_TOKEN);
    }

}
