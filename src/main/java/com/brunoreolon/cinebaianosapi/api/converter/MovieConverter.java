package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieVoteDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeSummaryResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
import com.brunoreolon.cinebaianosapi.util.PosterPathUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class MovieConverter {

    private final ModelMapper modelMapper;
    private final PosterPathUtil pathUtil;

    public MovieWithChooserResponse toWithChooserResponse(Movie movie) {
        MovieWithChooserResponse map = modelMapper.map(movie, MovieWithChooserResponse.class);
        map.setPosterPath(pathUtil.fullPosterPath(map.getPosterPath()));

        return map;
    }

    public MovieDetailResponse toDetailResponse(Movie movie) {
        MovieDetailResponse map = modelMapper.map(movie, MovieDetailResponse.class);
        map.setPosterPath(pathUtil.fullPosterPath(map.getPosterPath()));

        return map;
    }

    public MovieVoteDetailResponse toMovieVoteDetailResponse(Movie movie, String chooserDiscordId) {
        MovieVoteDetailResponse response = modelMapper.map(movie, MovieVoteDetailResponse.class);
        String posterPath = response.getMovie().getPosterPath();
        response.getMovie().setPosterPath(pathUtil.fullPosterPath(posterPath));

        Vote vote = movie.getVotes().stream()
                .filter(v -> v.getVoter().getDiscordId().equals(chooserDiscordId))
                .findFirst()
                .orElse(null);

        if (vote != null) {
            VoteTypeSummaryResponse voteTypeSummaryResponse = new VoteTypeSummaryResponse(
                    vote.getVote().getId(),
                    vote.getVote().getDescription(),
                    vote.getVote().getColor(),
                    vote.getVote().getEmoji()
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
