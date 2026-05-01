package com.brunoreolon.cinebaianosapi.domain.event;

import com.brunoreolon.cinebaianosapi.domain.model.GroupJoinRequest;
import com.brunoreolon.cinebaianosapi.domain.model.User;

import java.util.List;

public record GroupJoinRequestCreatedEvent(GroupJoinRequest request, List<User> recipients) {
}