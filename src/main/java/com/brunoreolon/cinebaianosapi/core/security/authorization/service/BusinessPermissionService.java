package com.brunoreolon.cinebaianosapi.core.security.authorization.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.exception.OwnershipAccessDeniedException;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@AllArgsConstructor
public class BusinessPermissionService {

    private final UserContextService userContextService;

    /**
     * Checa se o usuário pode adicionar filme para outro usuário
     */
    public void checkCanAddMovieFor(String chooserDiscordId) {
        User logged = userContextService.getLoggedUser()
                .orElseThrow(() -> new OwnershipAccessDeniedException("User not authenticated"));

        if (!userContextService.isBot() && !Objects.equals(logged.getDiscordId(), chooserDiscordId)) {
            throw new OwnershipAccessDeniedException("You cannot add movies for other users");
        }
    }

    /**
     * Checa se o usuário pode votar por outro usuário
     */
    public void checkCanVoteFor(String voterDiscordId) {
        User logged = userContextService.getLoggedUser()
                .orElseThrow(() -> new OwnershipAccessDeniedException("User not authenticated"));

        if (!userContextService.isBot() && !Objects.equals(logged.getDiscordId(), voterDiscordId)) {
            throw new OwnershipAccessDeniedException("You cannot vote on behalf of another user");
        }
    }

}
