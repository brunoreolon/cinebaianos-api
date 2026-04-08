package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupInviteResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupJoinRequestResponse;
import com.brunoreolon.cinebaianosapi.domain.model.GroupInvite;
import com.brunoreolon.cinebaianosapi.domain.model.GroupJoinRequest;
import org.springframework.stereotype.Component;

@Component
public class GroupAccessConverter {

    public GroupInviteResponse toInviteResponse(GroupInvite invite) {
        GroupInviteResponse response = new GroupInviteResponse();
        response.setId(invite.getId());
        response.setGroupId(invite.getGroup().getId());
        response.setToken(invite.getToken());
        response.setInviteType(invite.getInviteType());
        response.setStatus(invite.getStatus());
        response.setMaxUses(invite.getMaxUses());
        response.setUsesCount(invite.getUsesCount());
        response.setCreatedAt(invite.getCreatedAt());
        response.setExpiresAt(invite.getExpiresAt());
        response.setCreatedByUserId(invite.getCreatedBy().getId());
        response.setInvitedUserId(invite.getInvitedUser() != null ? invite.getInvitedUser().getId() : null);
        return response;
    }

    public GroupJoinRequestResponse toJoinRequestResponse(GroupJoinRequest joinRequest) {
        GroupJoinRequestResponse response = new GroupJoinRequestResponse();
        response.setId(joinRequest.getId());
        response.setGroupId(joinRequest.getGroup().getId());
        response.setUserId(joinRequest.getUser().getId());
        response.setStatus(joinRequest.getStatus());
        response.setCreatedAt(joinRequest.getCreatedAt());
        response.setReviewedByUserId(joinRequest.getReviewedBy() != null ? joinRequest.getReviewedBy().getId() : null);
        response.setReviewedAt(joinRequest.getReviewedAt());
        return response;
    }
}