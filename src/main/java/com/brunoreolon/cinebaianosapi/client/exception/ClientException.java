package com.brunoreolon.cinebaianosapi.client.exception;

import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class ClientException extends BusinessException {

    public ClientException(String message, HttpStatus status, ApiErrorCode errorCode) {
        super(message, status, "Client Exception", Map.of("errorCode", errorCode.getCode()));
    }

}
