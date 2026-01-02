package com.brunoreolon.cinebaianosapi.api.exceptionhandler;

import com.brunoreolon.cinebaianosapi.core.security.authorization.exception.OwnershipAccessDeniedException;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import com.brunoreolon.cinebaianosapi.util.ExceptionUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

import static com.brunoreolon.cinebaianosapi.util.ExceptionUtil.*;

@ControllerAdvice
@AllArgsConstructor
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String INVALID_FIELDS_TITLE = "validation.failed.title";
    public static final String INVALID_FIELDS_DETAIL = "validation.failed.detail";

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final MessageSource messageSource;

    private String msg(String code, Object[] args) {
        return messageSource.getMessage(
                code,
                args,
                LocaleContextHolder.getLocale()
        );
    }

    private Map<String, Object> translateInvalidFields(Map<String, Object> fields) {
        return fields.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> msg(entry.getValue().toString(), null)
                ));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
                                                                  HttpStatusCode status, WebRequest request) {
        Map<String, Object> invalidFields = translateInvalidFields(ExceptionUtil.getInvalidFields(ex));

        ProblemDetail problemDetail = getProblemDetail(
                msg(INVALID_FIELDS_TITLE, null),
                msg(INVALID_FIELDS_DETAIL, null),
                status,
                null,
                invalidFields
        );

        logger.warn("Validation failed for fields: {}", invalidFields, ex);
        return handleExceptionInternal(ex, problemDetail, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, Object> invalidFields = translateInvalidFields(ExceptionUtil.getInvalidFields(ex));

        ProblemDetail problem = getProblemDetail(
                msg(INVALID_FIELDS_TITLE, null),
                msg(INVALID_FIELDS_DETAIL, null),
                HttpStatus.BAD_REQUEST,
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
                msg(ex.getTitleKey(), null),
                msg(ex.getMessageKey(), ex.getArgs()),
                ex.getStatus(),
                ex.getProperties(),
                null
        );

        return ResponseEntity.status(ex.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(OwnershipAccessDeniedException.class)
    public ResponseEntity<Object> handleOwnershipAccessDeniedException(OwnershipAccessDeniedException ex) {
        logger.warn("OwnershipAccessDeniedException exception occurred: {}", ex.getMessage());

        ProblemDetail problemDetail = getProblemDetail(
                msg(ex.getTitleKey(), null),
                msg(ex.getMessageKey(), null),
                ex.getStatus(),
                ex.getErrorCode(),
                null
        );

        return ResponseEntity.status(ex.getStatus()).body(problemDetail);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ProblemDetail> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Invalid credentials attempt: {}", ex.getMessage());

        ProblemDetail problemDetail = ExceptionUtil.getProblemDetail(
                msg("auth.invalid_credentials.title", null),
                msg("auth.invalid_credentials.message", null),
                HttpStatus.UNAUTHORIZED,
                ApiErrorCode.INVALID_CREDENTIALS.asMap(),
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleAllExceptions(Exception ex) {
        logger.error("Unexpected exception occurred", ex);

        ProblemDetail problem = getProblemDetail(
                msg("internal.error.title", null),
                msg(ex.getMessage(), null),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCode.UNEXPECTED_ERROR.asMap(),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }

}