package com.brunoreolon.cinebaianosapi.core.security.authorization.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.Ownable;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.OwnableService;
import com.brunoreolon.cinebaianosapi.domain.model.Role;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Serviço responsável por decisões de autorização baseadas
 * no contexto do usuário autenticado.
 *
 * <p>Centraliza verificações como:
 * <ul>
 *   <li>roles</li>
 *   <li>admin</li>
 *   <li>bot</li>
 *   <li>ownership</li>
 * </ul>
 *
 * <p>Não contém regras de negócio, apenas regras de acesso.</p>
 */
@Component
@AllArgsConstructor
public class AuthorizationService {

    private final UserContextService userContextService;

    /**
     * Retorna true se o usuário atual for bot do discord
     */
    public boolean isBot() {
        return userContextService.isBot();
    }

    /**
     * Retorna true se o usuário atual for administrador
     */
    public boolean isAdmin() {
        return userContextService.isAdmin();
    }

    /**
     * Retorna true se o usuário atual possui qualquer uma das roles fornecidas
     */
    public boolean hasAnyRole(Role[] roles) {
        return userContextService.getLoggedUser()
                .map(user -> user.getRoles().stream()
                        .anyMatch(r -> Arrays.asList(roles).contains(r)))
                .orElse(false);
    }

    /**
     * Retorna true se o usuário atual é dono do recurso
     */
    public boolean isOwner(OwnableService service, Object ResourceKey) {
        User loggedUser = userContextService.getLoggedUser().orElse(null);
        if (loggedUser == null) return false;

        Object entity = service.get(ResourceKey);

        if (entity instanceof Ownable ownable) {
            return ownable.getOwnerId().equals(loggedUser.getDiscordId());
        }

        return false;
    }

}
