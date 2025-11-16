package com.brunoreolon.cinebaianosapi.api.model.vote.response;

import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VoteDetailResponse {

    private MovieSummaryResponse movie;
    private UserSummaryResponse voter;
    private VoteSummaryResponse vote;

}
