package com.brunoreolon.cinebaianosapi.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String title;
    private final Map<String, Object> properties;

    public BusinessException(String message, HttpStatus status, String title) {
        this(message, status, title, null, null);
    }

    public BusinessException(String message, HttpStatus status, String title, Map<String, Object> properties) {
        this(message, status, title, properties, null);
    }

    public BusinessException(String message, HttpStatus status, String title, Throwable cause) {
        this(message, status, title, null, cause);
    }

    public BusinessException(String message, HttpStatus status, String title, Map<String, Object> properties, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.title = title;
        this.properties = properties;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

}