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
    private String userName;
    private String userAvatar;
    private GroupJoinRequestStatus status;
    private LocalDateTime createdAt;
    private Long reviewedByUserId;
    private String reviewedByUserName;
    private LocalDateTime reviewedAt;

}