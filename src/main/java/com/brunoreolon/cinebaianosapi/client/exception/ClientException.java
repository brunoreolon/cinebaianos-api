package com.brunoreolon.cinebaianosapi.client.exception;

import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class ClientException extends BusinessException {

    public ClientException(String messageKey, String titleKey, HttpStatus status, ApiErrorCode errorCode) {
        super(messageKey, titleKey, status, errorCode.asMap());
    }

    public ClientException(String messageKey, String titleKey, Object[] args, HttpStatus status, ApiErrorCode errorCode) {
        super(messageKey, titleKey, args, status, errorCode.asMap());
    }

}
