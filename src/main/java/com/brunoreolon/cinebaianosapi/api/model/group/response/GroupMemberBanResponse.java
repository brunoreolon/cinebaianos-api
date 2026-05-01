package com.brunoreolon.cinebaianosapi.api.model.group.response;

import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GroupMemberBanResponse {

    private Long id;
    private UserSummaryResponse member;
    private UserSummaryResponse bannedBy;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

}