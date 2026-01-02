package com.brunoreolon.cinebaianosapi.core.security.authorization.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.AuthenticationFacade;
import com.brunoreolon.cinebaianosapi.core.security.authorization.exception.OwnershipAccessDeniedException;
import com.brunoreolon.cinebaianosapi.domain.model.CustomUserDetails;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Serviço responsável por fornecer informações
 * sobre o usuário autenticado no contexto atual.
 *
 * <p>Converte dados técnicos de autenticação
 * em informações do domínio (User),
 * centralizando regras como bot, admin e autenticação.</p>
 */

@Component
@AllArgsConstructor
public class UserContextService {

    private final AuthenticationFacade authenticationFacade;

    /**
     * Retorna o usuário atualmente autenticado
     */
    public Optional<User> getLoggedUser() {
        Authentication auth = authenticationFacade.getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails)
            return Optional.ofNullable(userDetails.getUser());

        return Optional.empty();
    }

    public boolean isAuthenticated() {
        return getLoggedUser().isPresent();
    }

    /**
     * Retorna true se o usuário atual for bot do discord
     */
    public boolean isBot() {
        return getLoggedUser().map(User::getIsBot).orElse(false);
    }

    /**
     * Retorna true se o usuário atual for administrador
     */
    public boolean isAdmin() {
        return getLoggedUser().map(User::isAdmin).orElse(false);
    }

    public User requireLoggedUser() {
        return getLoggedUser().orElseThrow(() ->
                new OwnershipAccessDeniedException("auth.user_not_authenticated"));
    }

}