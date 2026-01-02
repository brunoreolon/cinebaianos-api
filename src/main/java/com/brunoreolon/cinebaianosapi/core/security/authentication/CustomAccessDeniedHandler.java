package com.brunoreolon.cinebaianosapi.core.security.authentication;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import com.brunoreolon.cinebaianosapi.util.ExceptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.Locale;

@Component
@AllArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    private String msg(HttpServletRequest request, String code) {
        Locale locale = localeResolver.resolveLocale(request);
        return messageSource.getMessage(code, null, locale);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ProblemDetail problemDetail = ExceptionUtil.getProblemDetail(
                msg(request, "access.denied.title"),
                accessDeniedException.getMessage(),
                HttpStatus.FORBIDDEN,
                ApiErrorCode.USER_NOT_AUTHORIZED.asMap(),
                null
        );

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");

        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }
}
