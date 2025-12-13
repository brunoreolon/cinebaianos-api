package com.brunoreolon.cinebaianosapi.core.security.authentication;

import com.brunoreolon.cinebaianosapi.core.security.authentication.service.JwtService;
import com.brunoreolon.cinebaianosapi.domain.service.CustomUserDetailsService;
import com.brunoreolon.cinebaianosapi.domain.service.JwtBlacklistService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/auth",
            "/actuator/health",
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-ui.html"
    );

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.toLowerCase().startsWith("bearer ")) {
            logger.warn("Authorization header missing");
            request.setAttribute("authErrorReason", AuthErrorReason.MISSING_TOKEN);
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();

        if (jwtBlacklistService.isBlacklisted(token)) {
            logger.warn("JWT blacklisted");
            request.setAttribute("authErrorReason", AuthErrorReason.BLACKLISTED);
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.parseClaims(token);
            String username = claims.getSubject();

            var userDetails = userDetailsService.loadUserByUsername(username);
            var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (ExpiredJwtException e) {
            logger.info("JWT expired for user: {}", e.getClaims().getSubject());
            request.setAttribute("authErrorReason", AuthErrorReason.EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            logger.info("JWT invalid: {}", e.getMessage());
            request.setAttribute("authErrorReason", AuthErrorReason.INVALID);
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

}
