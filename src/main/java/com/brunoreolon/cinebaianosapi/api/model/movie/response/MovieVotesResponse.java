package com.brunoreolon.cinebaianosapi.api.model.movie.response;

import com.brunoreolon.cinebaianosapi.api.model.vote.response.UsersVotesSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class MovieVotesResponse {

    private MovieWithChooserResponse movie;
    private List<UsersVotesSummaryResponse> votes;

}
