package com.brunoreolon.cinebaianosapi.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionUtil {

    public static Map<String, Object> getInvalidFields(MethodArgumentNotValidException ex, MessageSource messageSource) {
        return ex.getBindingResult().getAllErrors().stream()
                .filter(error -> error instanceof FieldError)
                .collect(Collectors.toMap(
                        objectError -> ((FieldError) objectError).getField(),
                        objectError -> messageSource.getMessage(objectError, LocaleContextHolder.getLocale()))
                );
    }

    public static Map<String, Object> getInvalidFields(HandlerMethodValidationException ex, MessageSource messageSource) {
        Map<String, Object> errors = new HashMap<>();

        ex.getAllValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();

            result.getResolvableErrors().forEach(error -> {
                String message = error.getDefaultMessage();
                errors.put(paramName, message);
            });
        });

        return errors;
    }

    public static ProblemDetail getProblemDetail(HttpStatusCode status, String title, String detail,
                                                 Map<String, Object> additionalProperties, Map<String, Object> invalidFields) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);

        if (additionalProperties != null && !additionalProperties.isEmpty()) {
            additionalProperties.forEach(problemDetail::setProperty);
        }

        if (invalidFields != null && !invalidFields.isEmpty()) {
            problemDetail.setProperty("fields", invalidFields);
        }

        return problemDetail;
    }

}
