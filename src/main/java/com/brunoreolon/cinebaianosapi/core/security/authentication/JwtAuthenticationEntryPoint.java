package com.brunoreolon.cinebaianosapi.core.security.authentication;

import com.brunoreolon.cinebaianosapi.util.ExceptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.util.Locale;

@Component
@AllArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    private String msg(HttpServletRequest request, String code) {
        Locale locale = localeResolver.resolveLocale(request);
        return messageSource.getMessage(code, null, locale);
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        AuthErrorReason reason = AuthErrorReason.INVALID;

        Object attr = request.getAttribute("authErrorReason");
        if (attr instanceof AuthErrorReason r) {
            reason = r;
        }

        ProblemDetail problemDetail = ExceptionUtil.getProblemDetail(
                msg(request, reason.getTitleKey()),
                msg(request, reason.getMessageKey()),
                HttpStatus.UNAUTHORIZED,
                reason.getCodeAsMap(),
                null
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }

}