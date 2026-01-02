package com.brunoreolon.cinebaianosapi.domain.event;

import com.brunoreolon.cinebaianosapi.domain.model.User;

public record UserCreatedEvent(User user, String password) {
}