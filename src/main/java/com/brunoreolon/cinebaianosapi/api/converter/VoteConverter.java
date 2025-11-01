package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieVotesResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserMovieVoteResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.UsersVotesSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeSummaryResponse;
import com.brunoreolon.cinebaianosapi.domain.model.MovieVotes;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
import com.brunoreolon.cinebaianosapi.util.PosterPathUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class VoteConverter {

    private final ModelMapper modelMapper;
    private final PosterPathUtil pathUtil;

    public VoteDetailResponse toDetailResponse(Vote movie) {
        return modelMapper.map(movie, VoteDetailResponse.class);
    }

    public UserMovieVoteResponse toUserMovieVoteResponse(Vote vote) {
        return modelMapper.map(vote, UserMovieVoteResponse.class);
    }

    public List<UserMovieVoteResponse> toUserMovieVoteResponseList(List<Vote> votes) {
        return votes.stream()
                .map(this::toUserMovieVoteResponse)
                .toList();
    }

    public MovieVotesResponse toMovieVotesResponse(MovieVotes movieVotes) {
        List<UsersVotesSummaryResponse> votes = movieVotes.getVotes().stream()
                .map(v -> {
                    UserSummaryResponse voter = modelMapper.map(v.getVoter(), UserSummaryResponse.class);
                    VoteTypeSummaryResponse vote = modelMapper.map(v.getVote(), VoteTypeSummaryResponse.class);

                    return new UsersVotesSummaryResponse(voter, vote);
                })
                .toList();

        MovieWithChooserResponse movieWithChooserResponse = modelMapper.map(movieVotes.getMovie(), MovieWithChooserResponse.class);
        movieWithChooserResponse.setPosterPath(pathUtil.fullPosterPath(movieWithChooserResponse.getPosterPath()));

        return new MovieVotesResponse(movieWithChooserResponse, votes);
    }

}
