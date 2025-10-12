package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieVoteDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeSummaryResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class MovieConverter {

    private final ModelMapper modelMapper;

    public MovieWithChooserResponse toWithChooserResponse(Movie movie) {
        return modelMapper.map(movie, MovieWithChooserResponse.class);
    }

    public MovieDetailResponse toDetailResponse(Movie movie) {
        return modelMapper.map(movie, MovieDetailResponse.class);
    }

    public MovieVoteDetailResponse toMovieVoteDetailResponse(Movie movie, String chooserDiscordId) {
        MovieVoteDetailResponse response = modelMapper.map(movie, MovieVoteDetailResponse.class);

        Vote vote = movie.getVotes().stream()
                .filter(v -> v.getVoter().getDiscordId().equals(chooserDiscordId))
                .findFirst()
                .orElse(null);

        if (vote != null) {
            VoteTypeSummaryResponse voteTypeSummaryResponse = new VoteTypeSummaryResponse(
                    vote.getVote().getId(),
                    vote.getVote().getDescription()
            );

            response.setVote(voteTypeSummaryResponse);
        }

        return response;
    }

    public List<MovieWithChooserResponse> toWithChooserResponseList(List<Movie> movies) {
        return movies.stream()
                .map(this::toWithChooserResponse)
                .toList();
    }

}
