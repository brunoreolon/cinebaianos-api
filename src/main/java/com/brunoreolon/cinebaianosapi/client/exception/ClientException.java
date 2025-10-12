package com.brunoreolon.cinebaianosapi.client.exception;

import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import org.springframework.http.HttpStatus;

public class ClientException extends BusinessException {

    public ClientException(String message, HttpStatus status) {
        super(message, status, "Client Exception");
    }

}
