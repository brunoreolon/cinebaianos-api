package com.brunoreolon.cinebaianosapi.api.model.movie.response;

import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MovieVoteDetailResponse {

    private MovieDetailResponse movie;
    private UserSummaryResponse chooser;
    private VoteTypeSummaryResponse vote;

}
