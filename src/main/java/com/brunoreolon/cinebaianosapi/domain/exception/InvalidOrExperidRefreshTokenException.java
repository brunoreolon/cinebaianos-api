package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidOrExperidRefreshTokenException extends BusinessException {

    public static final String INVALID_OR_EXPIRED_REFRESH_TOKEN = "Invalid or Expired refresh token";

    public InvalidOrExperidRefreshTokenException(String message, ApiErrorCode apiErrorCode) {
        super(message, HttpStatus.UNAUTHORIZED, INVALID_OR_EXPIRED_REFRESH_TOKEN, apiErrorCode.asMap());
    }

}
