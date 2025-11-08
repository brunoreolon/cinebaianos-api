package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ExperidRefreshTokenException extends InvalidOrExperidRefreshTokenException {

    public ExperidRefreshTokenException(String message) {
        super(message, ApiErrorCode.EXPIRED_REFRESH_TOKEN);
    }

}
