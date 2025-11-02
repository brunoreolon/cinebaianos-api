package com.brunoreolon.cinebaianosapi.core.security;

import com.brunoreolon.cinebaianosapi.domain.model.*;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("authz")
@AllArgsConstructor
public class ApplicationService {

    public User getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser();
        }

        return null;
    }

    public boolean isBot() {
        return getLoggedUser() != null && getLoggedUser().isBot();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isOwner(OwnableService service, Object resourceId) {
        User logged = getLoggedUser();
        Object entity = service.get(resourceId);

        if (entity instanceof Ownable ownable) {
            return ownable.getOwnerId().equals(logged.getDiscordId());
        }
        return false;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean isOwnerOrBot(OwnableService service, Object resourceId) {
        if (isBot()) return true;
        return isOwner(service, resourceId);
    }

    public void checkCanAddMovieFor(String chooserDiscordId) {
        User logged = getLoggedUser();
        if (logged == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (!isBot() && !logged.getDiscordId().equals(chooserDiscordId)) {
            throw new AccessDeniedException("You cannot add movies for other users");
        }
    }

    public void checkCanVoteFor(String voterDiscordId) {
        User logged = getLoggedUser();
        if (logged == null) {
            throw new AccessDeniedException("User not authenticated");
        }

        if (!isBot() && !logged.getDiscordId().equals(voterDiscordId)) {
            throw new AccessDeniedException("You cannot vote on behalf of another user");
        }
    }

}
