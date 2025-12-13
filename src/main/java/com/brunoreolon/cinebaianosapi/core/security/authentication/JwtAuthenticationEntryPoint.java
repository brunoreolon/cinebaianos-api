package com.brunoreolon.cinebaianosapi.core.security.authentication;

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
        AuthErrorReason reason = AuthErrorReason.INVALID;

        Object attr = request.getAttribute("authErrorReason");
        if (attr instanceof AuthErrorReason r) {
            reason = r;
        }

        ProblemDetail problemDetail = ExceptionUtil.getProblemDetail(
                HttpStatus.UNAUTHORIZED,
                reason.getTitle(),
                reason.getDefaultDetail(),
                reason.getCodeAsMap(),
                null
        );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }

}
