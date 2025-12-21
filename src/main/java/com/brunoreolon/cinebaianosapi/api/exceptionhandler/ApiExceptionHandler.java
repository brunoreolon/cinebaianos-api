package com.brunoreolon.cinebaianosapi.api.exceptionhandler;

import com.brunoreolon.cinebaianosapi.core.security.authorization.exception.OwnershipAccessDeniedException;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import com.brunoreolon.cinebaianosapi.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

import static com.brunoreolon.cinebaianosapi.util.ExceptionUtil.*;

@ControllerAdvice
@AllArgsConstructor
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String INVALID_FIELDS_TITLE = "One or more fields are invalid";
    public static final String INVALID_FIELDS_DETAIL = "Check the 'fields' property for details";

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

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

        logger.warn("Validation failed for fields: {}", invalidFields, ex);
        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, Object> invalidFields = getInvalidFields(ex, messageSource);
        ProblemDetail problem = getProblemDetail(
                HttpStatus.BAD_REQUEST,
                INVALID_FIELDS_TITLE,
                INVALID_FIELDS_DETAIL,
                null,
                invalidFields
        );

        logger.warn("Validation failed for fields: {}", invalidFields, ex);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Object> handleBusinessException(BusinessException ex) {
        logger.warn("Business exception occurred: {}", ex.getMessage());

        ProblemDetail problemDetail = getProblemDetail(
                ex.getStatus(),
                ex.getTitle(),
                ex.getMessage(),
                ex.getProperties(),
                null
        );

        return ResponseEntity.status(ex.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(OwnershipAccessDeniedException.class)
    public ResponseEntity<Object> handleOwnershipAccessDeniedException(OwnershipAccessDeniedException ex) {
        logger.warn("OwnershipAccessDeniedException exception occurred: {}", ex.getMessage());

        ProblemDetail problemDetail = getProblemDetail(
                ex.getStatus(),
                ex.getTitle(),
                ex.getMessage(),
                ex.getErrorCode(),
                null
        );

        return ResponseEntity.status(ex.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Invalid credentials attempt: {}", ex.getMessage());

        ProblemDetail problemDetail = ExceptionUtil.getProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Invalid credentials",
                ex.getMessage(),
                ApiErrorCode.INVALID_CREDENTIALS.asMap(),
                null
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAllExceptions(Exception ex) {
        logger.error("Unexpected exception occurred", ex);

        ProblemDetail problem = getProblemDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                ApiErrorCode.UNEXPECTED_ERROR.asMap(),
                null
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

}
