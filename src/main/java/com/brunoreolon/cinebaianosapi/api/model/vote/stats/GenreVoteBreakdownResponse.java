package com.brunoreolon.cinebaianosapi.api.model.vote.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GenreVoteBreakdownResponse {

    private String genre;
    private List<VoteStatsResponse> votes;

}
