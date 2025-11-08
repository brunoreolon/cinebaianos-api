package com.brunoreolon.cinebaianosapi.core.security;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import com.brunoreolon.cinebaianosapi.util.ExceptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@AllArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ProblemDetail problemDetail = ExceptionUtil.getProblemDetail(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                authException.getMessage(),
                ApiErrorCode.INVALID_OR_EXPIRED_ACCESS_TOKEN.asMap(),
                null
        );


        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }
}
