package com.brunoreolon.cinebaianosapi.api.exceptionhandler;

import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

import static com.brunoreolon.cinebaianosapi.util.ExceptionUtil.*;

@ControllerAdvice
@AllArgsConstructor
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String INVALID_FIELDS_TITLE = "One or more fields are invalid";
    public static final String INVALID_FIELDS_DETAIL = "Check the 'fields' property for details";

    private final MessageSource messageSource;

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, Object> invalidFields = getInvalidFields(ex, messageSource);
        ProblemDetail problemDetail = getProblemDetail(
                status,
                INVALID_FIELDS_TITLE,
                INVALID_FIELDS_DETAIL,
                null,
                invalidFields
        );

        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex) {
        ProblemDetail problemDetail = getProblemDetail(
                ex.getStatus(),
                ex.getTitle(),
                ex.getMessage(),
                ex.getProperties(),
                null
        );

        return ResponseEntity.status(ex.getStatus()).body(problemDetail);
    }

}
