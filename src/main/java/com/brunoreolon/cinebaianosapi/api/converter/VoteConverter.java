package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieVotesResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserMovieVoteResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.UsersVotesSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteSummaryResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.MovieVotes;
import com.brunoreolon.cinebaianosapi.domain.model.User;
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

//    public VoteDetailResponse toDetailResponse(Vote movie) {
//        return modelMapper.map(movie, VoteDetailResponse.class);
//    }

    public VoteDetailResponse toDetailResponse(Vote vote) {
        Movie movie = vote.getMovie();
        User voter = vote.getVoter();
        VoteSummaryResponse voteSummary = new VoteSummaryResponse(
                vote.getVote().getId(),
                vote.getVote().getDescription(),
                vote.getVote().getColor(),
                vote.getVote().getEmoji(),
                vote.getCreated()
        );

        return new VoteDetailResponse(
                new MovieSummaryResponse(movie.getId(), movie.getTitle(), movie.getTmdbId()),
                new UserSummaryResponse(voter.getDiscordId(), voter.getName()),
                voteSummary
        );
    }

    public UserMovieVoteResponse toUserMovieVoteResponse(Vote vote) {
        MovieSummaryResponse map1 = new MovieSummaryResponse(vote.getMovie().getId(), vote.getMovie().getTitle(), vote.getMovie().getYear());
        VoteSummaryResponse map2 = modelMapper.map(vote, VoteSummaryResponse.class);

        return new UserMovieVoteResponse(map1, map2);
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
                    VoteSummaryResponse vote = modelMapper.map(v.getVote(), VoteSummaryResponse.class);
                    vote.setVotedAt(v.getCreated());

                    return new UsersVotesSummaryResponse(voter, vote);
                })
                .toList();

        MovieWithChooserResponse movieWithChooserResponse = modelMapper.map(movieVotes.getMovie(), MovieWithChooserResponse.class);
        movieWithChooserResponse.setPosterPath(pathUtil.fullPosterPath(movieWithChooserResponse.getPosterPath()));

        return new MovieVotesResponse(movieWithChooserResponse, votes);
    }

}
