package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupInviteCandidateResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupInviteCandidateSliceResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupInviteResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupJoinRequestResponse;
import com.brunoreolon.cinebaianosapi.domain.model.GroupInvite;
import com.brunoreolon.cinebaianosapi.domain.model.GroupJoinRequest;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class GroupAccessConverter {

    public GroupInviteResponse toInviteResponse(GroupInvite invite) {
        GroupInviteResponse response = new GroupInviteResponse();
        response.setId(invite.getId());
        response.setGroupId(invite.getGroup().getId());
        response.setGroupName(invite.getGroup().getName());
        response.setGroupTag(invite.getGroup().getTag());
        response.setToken(invite.getToken());
        response.setInviteType(invite.getInviteType());
        response.setStatus(invite.getStatus());
        response.setMaxUses(invite.getMaxUses());
        response.setUsesCount(invite.getUsesCount());
        response.setCreatedAt(invite.getCreatedAt());
        response.setExpiresAt(invite.getExpiresAt());
        response.setCreatedByUserId(invite.getCreatedBy().getId());
        response.setCreatedByUserName(invite.getCreatedBy().getName());
        response.setInvitedUserId(invite.getInvitedUser() != null ? invite.getInvitedUser().getId() : null);
        response.setInvitedUserName(invite.getInvitedUser() != null ? invite.getInvitedUser().getName() : null);
        response.setInvitedUserAvatar(invite.getInvitedUser() != null ? invite.getInvitedUser().getAvatar() : null);
        return response;
    }

    public GroupJoinRequestResponse toJoinRequestResponse(GroupJoinRequest joinRequest) {
        GroupJoinRequestResponse response = new GroupJoinRequestResponse();
        response.setId(joinRequest.getId());
        response.setGroupId(joinRequest.getGroup().getId());
        response.setUserId(joinRequest.getUser().getId());
        response.setUserName(joinRequest.getUser().getName());
        response.setUserAvatar(joinRequest.getUser().getAvatar());
        response.setStatus(joinRequest.getStatus());
        response.setCreatedAt(joinRequest.getCreatedAt());
        response.setReviewedByUserId(joinRequest.getReviewedBy() != null ? joinRequest.getReviewedBy().getId() : null);
        response.setReviewedByUserName(joinRequest.getReviewedBy() != null ? joinRequest.getReviewedBy().getName() : null);
        response.setReviewedAt(joinRequest.getReviewedAt());
        return response;
    }

    public GroupInviteCandidateResponse toInviteCandidateResponse(User user) {
        GroupInviteCandidateResponse response = new GroupInviteCandidateResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAvatar(user.getAvatar());
        return response;
    }

    public GroupInviteCandidateSliceResponse toInviteCandidateSliceResponse(Slice<User> usersSlice) {
        return new GroupInviteCandidateSliceResponse(
                usersSlice.getNumber(),
                usersSlice.getSize(),
                usersSlice.getNumberOfElements(),
                usersSlice.hasNext(),
                usersSlice.getContent().stream()
                        .map(this::toInviteCandidateResponse)
                        .toList()
        );
    }
}