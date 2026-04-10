package com.brunoreolon.cinebaianosapi.core.security.authorization.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.interfaces.GroupAuthorizationService;
import com.brunoreolon.cinebaianosapi.core.security.authorization.interfaces.Ownable;
import com.brunoreolon.cinebaianosapi.core.security.authorization.interfaces.OwnableService;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.UserRole;
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
@AllArgsConstructor
@Component
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
     * Retorna o ID do usuário logado ou null se nenhum usuário estiver autenticado
     */
    public Long getCurrentUserId() {
        return userContextService.getLoggedUser()
                .map(User::getId)
                .orElse(null);
    }

    /**
     * Retorna true se o usuário atual possui qualquer uma das roles fornecidas
     */
    public boolean hasAnyRole(UserRole[] userRoles) {
        return userContextService.getLoggedUser()
                .map(user -> user.getRoles().stream()
                        .anyMatch(r -> Arrays.asList(userRoles).contains(r)))
                .orElse(false);
    }

    /**
     * Retorna true se o usuário atual possui a role mínima exigida (ou superior)
     */
    public boolean hasMinimalRole(UserRole role) {
        User loggedUser = userContextService.getLoggedUser().orElse(null);
        if (loggedUser == null) return false;

        return loggedUser.getRoles().stream()
                .anyMatch(ur -> ur.atLeast(role));
    }

    /**
     * Retorna true se o usuário atual é dono do recurso
     */
    public boolean isOwner(OwnableService service, Object resourceKey) {
        User loggedUser = userContextService.getLoggedUser().orElse(null);
        if (loggedUser == null) return false;

        Object entity = service.get(resourceKey);

        if (entity instanceof Ownable ownable) {
            return ownable.getOwnerId().equals(loggedUser.getId());
        }

        return false;
    }

    public boolean authorize(GroupAuthorizationService service, Long groupId, GroupMemberRole requiredRole) {
        User loggedUser = userContextService.getLoggedUser().orElse(null);
        if (loggedUser == null) return false;

        if (service.isBanned(groupId, loggedUser.getId())) {
            return false;
        }

        if (requiredRole == null) {
            return service.isMember(groupId, loggedUser.getId());
        } else {
            return service.hasRole(groupId, loggedUser.getId(), requiredRole);
        }
    }

}