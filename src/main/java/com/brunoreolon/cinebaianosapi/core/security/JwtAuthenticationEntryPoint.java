package com.brunoreolon.cinebaianosapi.core.security;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import com.brunoreolon.cinebaianosapi.util.ExceptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String authError = (String) request.getAttribute("authError");
        if (authError == null)
            authError = "Access token missing or invalid";

        ProblemDetail problemDetail = ExceptionUtil.getProblemDetail(
                HttpStatus.UNAUTHORIZED,
                authError.equals("Token expired") ? "Access token expired" : "Unauthorized",
                authError,
                ApiErrorCode.INVALID_OR_EXPIRED_ACCESS_TOKEN.asMap(),
                null
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }

}
