package com.brunoreolon.cinebaianosapi.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
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

    public static Map<String, Object> getInvalidFields(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getAllErrors().stream()
                .filter(error -> error instanceof FieldError)
                .collect(Collectors.toMap(
                        error -> ((FieldError) error).getField(),
                        error -> error.getDefaultMessage()
                ));
    }

    public static Map<String, Object> getInvalidFields(HandlerMethodValidationException ex) {
        Map<String, Object> errors = new HashMap<>();

        ex.getAllValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();

            result.getResolvableErrors().forEach(error -> {
                errors.put(paramName, error.getDefaultMessage());
            });
        });

        return errors;
    }

    public static ProblemDetail getProblemDetail(
            String title,
            String detail,
            HttpStatusCode status,
            Map<String, Object> additionalProperties,
            Map<String, Object> invalidFields
    ) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);

        if (additionalProperties != null) {
            additionalProperties.forEach(problemDetail::setProperty);
        }

        if (invalidFields != null && !invalidFields.isEmpty()) {
            problemDetail.setProperty("fields", invalidFields);
        }

        return problemDetail;
    }
}