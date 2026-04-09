package com.brunoreolon.cinebaianosapi.api.model.group.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GroupInviteCreateRequest {

    private Long invitedUserId;

    @Positive
    private Integer maxUses;

    @Future
    private LocalDateTime expiresAt;

}