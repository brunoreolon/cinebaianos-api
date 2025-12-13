package com.brunoreolon.cinebaianosapi.core.security.authorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Fachada de acesso ao {@link SecurityContextHolder}.
 *
 * <p>Isola o uso direto do Spring Security,
 * permitindo desacoplamento e facilidade de testes.</p>
 */

@Component
public class AuthenticationFacade {

    public Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

}
