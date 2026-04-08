package com.brunoreolon.cinebaianosapi.api.model.group.response;

import com.brunoreolon.cinebaianosapi.domain.model.GroupInviteStatus;
import com.brunoreolon.cinebaianosapi.domain.model.GroupInviteType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GroupInviteResponse {

    private Long id;
    private Long groupId;
    private String token;
    private GroupInviteType inviteType;
    private GroupInviteStatus status;
    private Integer maxUses;
    private Integer usesCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Long createdByUserId;
    private Long invitedUserId;
}