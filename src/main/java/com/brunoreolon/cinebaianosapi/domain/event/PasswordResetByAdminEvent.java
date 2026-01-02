package com.brunoreolon.cinebaianosapi.domain.event;

import com.brunoreolon.cinebaianosapi.domain.model.User;

public record PasswordResetByAdminEvent(User user, String password) {
}
