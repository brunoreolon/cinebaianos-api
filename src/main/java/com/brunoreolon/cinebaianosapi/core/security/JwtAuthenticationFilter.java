package com.brunoreolon.cinebaianosapi.core.security;

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

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtBlacklistService jwtBlacklistService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.toLowerCase().startsWith("bearer ")) {
            String token = header.substring(7).trim();

            if (token.isBlank()) {
                logger.warn("Authorization header is blank");
                request.setAttribute("authError", "Token missing");
            } else if (jwtBlacklistService.isBlacklisted(token)) {
                logger.warn("JWT blacklisted");
                request.setAttribute("authError", "Token blacklisted");
            } else {
                try {
                    Claims claims = jwtService.parseClaims(token);
                    String username = claims.getSubject();

                    var userDetails = userDetailsService.loadUserByUsername(username);
                    var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (ExpiredJwtException e) {
                    logger.info("JWT expired for user: {}", e.getClaims().getSubject());
                    request.setAttribute("authError", "Token expired");
                } catch (JwtException | IllegalArgumentException e) {
                    logger.info("JWT invalid: {}", e.getMessage());
                    request.setAttribute("authError", "Token invalid");
                }
            }
        } else {
            request.setAttribute("authError", "Authorization header missing");
        }

        chain.doFilter(request, response);
    }
}
