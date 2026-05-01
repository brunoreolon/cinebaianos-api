package com.brunoreolon.cinebaianosapi.api.model.vote.response;

import com.brunoreolon.cinebaianosapi.api.model.user.response.UserDetailResponse;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsersVotesSummaryResponse {

    private UserDetailResponse voter;
    private GroupMembershipStatus voterMembershipStatus;
    private java.time.LocalDateTime voterBanExpiresAt;
    private VoteSummaryResponse vote;

}
