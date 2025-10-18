package com.brunoreolon.cinebaianosapi.api.model.vote.stats;

import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class VoteStatsResponse {

    private VoteTypeSummaryResponse type;
    private Long totalVotes;

}
