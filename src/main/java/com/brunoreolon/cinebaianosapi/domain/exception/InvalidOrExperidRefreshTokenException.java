package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class InvalidOrExperidRefreshTokenException extends BusinessException {

    public static final String INVALID_OR_EXPIRED_REFRESH_TOKEN = "invalid.expired.refres.token.title";

    public InvalidOrExperidRefreshTokenException(String message, ApiErrorCode apiErrorCode) {
        super(INVALID_OR_EXPIRED_REFRESH_TOKEN, message, HttpStatus.UNAUTHORIZED, apiErrorCode.asMap());
    }

}