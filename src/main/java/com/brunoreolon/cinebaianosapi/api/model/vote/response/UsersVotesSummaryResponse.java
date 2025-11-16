package com.brunoreolon.cinebaianosapi.api.model.vote.response;

import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UsersVotesSummaryResponse {

    private UserSummaryResponse voter;
    private VoteSummaryResponse vote;

}
