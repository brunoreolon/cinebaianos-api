package com.brunoreolon.cinebaianosapi.api.model.group.response;

import com.brunoreolon.cinebaianosapi.domain.model.GroupJoinRequestStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class GroupJoinRequestResponse {

    private Long id;
    private Long groupId;
    private Long userId;
    private GroupJoinRequestStatus status;
    private LocalDateTime createdAt;
    private Long reviewedByUserId;
    private LocalDateTime reviewedAt;

}