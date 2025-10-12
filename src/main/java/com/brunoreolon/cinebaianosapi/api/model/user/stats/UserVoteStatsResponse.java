package com.brunoreolon.cinebaianosapi.api.model.user.stats;

import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.stats.VoteStatsResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserVoteStatsResponse {

    private UserSummaryResponse user;
    private List<VoteStatsResponse> votes;

}
