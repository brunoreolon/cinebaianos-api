package com.brunoreolon.cinebaianosapi.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExceptionUtil {

    public static Map<String, List<String>> getInvalidFields(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getAllErrors().stream()
                .filter(error -> error instanceof FieldError)
                .map(error -> (FieldError) error)
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        LinkedHashMap::new, // mantém ordem
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));
    }

    public static Map<String, List<String>> getInvalidFields(HandlerMethodValidationException ex) {
        Map<String, List<String>> errors = new LinkedHashMap<>(); // mantém ordem

        ex.getAllValidationResults().forEach(result -> {
            String paramName = result.getMethodParameter().getParameterName();

            List<String> messages = errors.computeIfAbsent(paramName, k -> new ArrayList<>());

            result.getResolvableErrors().forEach(error -> messages.add(error.getDefaultMessage()));
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