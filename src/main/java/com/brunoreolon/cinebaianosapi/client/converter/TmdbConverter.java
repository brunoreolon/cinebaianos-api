package com.brunoreolon.cinebaianosapi.client.converter;

import com.brunoreolon.cinebaianosapi.client.TmdbProperties;
import com.brunoreolon.cinebaianosapi.client.model.MovieResponse;
import com.brunoreolon.cinebaianosapi.client.model.TmdbMovieResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@AllArgsConstructor
public class TmdbConverter {

    private final TmdbProperties tmdbProperties;
    private final ModelMapper modelMapper;

    public Movie toEntity(MovieResponse movieResponse) {
        return modelMapper.map(movieResponse, Movie.class);
    }

    public TmdbMovieResponse convert(TmdbMovieResponse tmdbMovieResponse) {
        TmdbMovieResponse map = modelMapper.map(tmdbMovieResponse, TmdbMovieResponse.class);
        if (tmdbMovieResponse.getPosterPath() != null) {
            map.setPosterPath(tmdbProperties.getPosterPath() + map.getPosterPath());
        }

        return map;
    }

    public List<TmdbMovieResponse> converteList(List<TmdbMovieResponse> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }

        return list.stream()
                .map(this::convert)
                .toList();
    }

    public MovieResponse convertMovieDetail(MovieResponse movieResponse) {
        MovieResponse map = modelMapper.map(movieResponse, MovieResponse.class);
        if (map.getPosterPath() != null) {
            map.setPosterPath(tmdbProperties.getPosterPath() + map.getPosterPath());
        }

        return map;
    }

}
