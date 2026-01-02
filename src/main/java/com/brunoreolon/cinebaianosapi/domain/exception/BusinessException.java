package com.brunoreolon.cinebaianosapi.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String titleKey;
    private final String messageKey;
    private final Object[] args;
    private final Map<String, Object> properties;

    public BusinessException(String titleKey, String messageKey, HttpStatus status) {
        this(titleKey, messageKey, null, status, null);
    }

    public BusinessException(
            String titleKey,
            String messageKey,
            HttpStatus status,
            Map<String, Object> properties
    ) {
        this(titleKey, messageKey, null, status, properties);
    }

    public BusinessException(
            String titleKey,
            String messageKey,
            Object[] args,
            HttpStatus status
    ) {
        this(titleKey, messageKey, args, status, null);
    }

    public BusinessException(
            String titleKey,
            String messageKey,
            Object[] args,
            HttpStatus status,
            Map<String, Object> properties
    ) {
        super(messageKey); // importante: NÃO traduz
        this.titleKey = titleKey;
        this.messageKey = messageKey;
        this.args = args;
        this.status = status;
        this.properties = properties;
    }
}