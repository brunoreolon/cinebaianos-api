package com.brunoreolon.cinebaianosapi.domain.event;

import com.brunoreolon.cinebaianosapi.domain.model.GroupInvite;
import com.brunoreolon.cinebaianosapi.domain.model.User;

public record GroupInviteAcceptedEvent(GroupInvite invite, User acceptedBy) {
}