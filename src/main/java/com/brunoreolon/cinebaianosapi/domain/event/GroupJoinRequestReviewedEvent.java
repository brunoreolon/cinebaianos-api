package com.brunoreolon.cinebaianosapi.domain.event;

import com.brunoreolon.cinebaianosapi.domain.model.GroupJoinRequest;

public record GroupJoinRequestReviewedEvent(GroupJoinRequest request, boolean approved) {
}