package com.brunoreolon.cinebaianosapi.core.security.authorization.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.exception.OwnershipAccessDeniedException;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@AllArgsConstructor
@Component
public class BusinessPermissionService {

    private final UserContextService userContextService;

    /**
     * Checa se o usuário pode adicionar filme para outro usuário
     */
    public void checkCanAddMovieFor(Long chooserId) {
        User logged = userContextService.getLoggedUser()
                .orElseThrow(() -> new OwnershipAccessDeniedException("auth.user_not_authenticated"));

        if (!userContextService.isBot() && !Objects.equals(logged.getId(), chooserId)) {
            throw new OwnershipAccessDeniedException("auth.cannot_add_movie_for_other");
        }
    }

    /**
     * Checa se o usuário pode votar por outro usuário
     */
    public void checkCanVoteFor(Long voterId) {
        User logged = userContextService.getLoggedUser()
                .orElseThrow(() -> new OwnershipAccessDeniedException("auth.user_not_authenticated"));

        if (!userContextService.isBot() && !Objects.equals(logged.getId(), voterId)) {
            throw new OwnershipAccessDeniedException("auth.cannot_vote_for_other");
        }
    }

}