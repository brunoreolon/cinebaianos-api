package com.brunoreolon.cinebaianosapi.api.converter;

import com.brunoreolon.cinebaianosapi.api.model.tmdb.TmdbMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.api.model.tmdb.TmdbMovieResponse;
import com.brunoreolon.cinebaianosapi.client.model.ClientMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.client.model.CrewResponse;
import com.brunoreolon.cinebaianosapi.client.model.ResultResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.util.PosterPathUtil;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class TmdbConverter {

    private final ModelMapper modelMapper;
    private final PosterPathUtil pathUtil;

    private String getDirector(ClientMovieDetailsResponse movieDetails) {
        return movieDetails.getCredits().getCrew().stream()
                .filter(c -> "Director".equalsIgnoreCase(c.getJob()))
                .map(CrewResponse::getName)
                .findFirst()
                .orElse(null);
    }

    public TmdbMovieDetailsResponse toMovieDetailsResponse(ClientMovieDetailsResponse clientMovieDetailsResponse) {
        TmdbMovieDetailsResponse map = modelMapper.map(clientMovieDetailsResponse, TmdbMovieDetailsResponse.class);
        map.setPosterPath(pathUtil.fullPosterPath(map.getPosterPath()));
        map.setDirector(getDirector(clientMovieDetailsResponse));

        return map;
    }

    public List<TmdbMovieDetailsResponse> toMovieDetailsResponseList(List<ClientMovieDetailsResponse> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        return list.stream()
                .map(this::toMovieDetailsResponse)
                .toList();
    }

    public List<TmdbMovieResponse> toMovieResponseList(List<ResultResponse> results) {
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        return results.stream()
                .map(this::toMovieResponse)
                .toList();
    }

    public TmdbMovieResponse toMovieResponse(ResultResponse resultResponse) {
        TmdbMovieResponse map = modelMapper.map(resultResponse, TmdbMovieResponse.class);
        map.setPosterPath(pathUtil.fullPosterPath(map.getPosterPath()));

        return map;
    }

    public Movie toEntityFromClientMovieDetail(ClientMovieDetailsResponse clientMovieDetailsResponse) {
        Movie map = modelMapper.map(clientMovieDetailsResponse, Movie.class);
        map.setDirector(getDirector(clientMovieDetailsResponse));

        return map;
    }

}
