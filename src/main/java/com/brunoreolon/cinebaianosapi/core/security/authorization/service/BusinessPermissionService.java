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
                .orElseThrow(() -> new OwnershipAccessDeniedException("auth.user_not_authenticated"));

        if (!userContextService.isBot() && !Objects.equals(logged.getDiscordId(), chooserDiscordId)) {
            throw new OwnershipAccessDeniedException("auth.cannot_add_movie_for_other");
        }
    }

    /**
     * Checa se o usuário pode votar por outro usuário
     */
    public void checkCanVoteFor(String voterDiscordId) {
        User logged = userContextService.getLoggedUser()
                .orElseThrow(() -> new OwnershipAccessDeniedException("auth.user_not_authenticated"));

        if (!userContextService.isBot() && !Objects.equals(logged.getDiscordId(), voterDiscordId)) {
            throw new OwnershipAccessDeniedException("auth.cannot_vote_for_other");
        }
    }

}
