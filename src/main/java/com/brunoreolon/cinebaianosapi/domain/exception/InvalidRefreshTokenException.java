package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidRefreshTokenException extends BusinessException {

    public InvalidRefreshTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED, "Invalid Refresh Token",
                Map.of("errorCode", ApiErrorCode.INVALID_OR_EXPIRED_REFRESH_TOKEN.getCode()));
    }

}
