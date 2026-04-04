package com.brunoreolon.cinebaianosapi.api.model.group.response;

import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMemberRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GroupMemberResponse {

    private UserSummaryResponse member;
    private GroupMemberRole role;
    private Boolean active;
    private Boolean selected;
    private LocalDateTime joinedAt;
    private LocalDateTime leftAt;

}

