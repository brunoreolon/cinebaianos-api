package com.brunoreolon.cinebaianosapi.api.model.user.response;

import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserMovieVoteResponse {

    private MovieSummaryResponse movie;
    private VoteSummaryResponse vote;

}
