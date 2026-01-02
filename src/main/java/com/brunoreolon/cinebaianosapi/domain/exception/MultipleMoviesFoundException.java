package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.api.model.tmdb.TmdbMovieResponse;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class MultipleMoviesFoundException extends BusinessException {

    public MultipleMoviesFoundException(List<TmdbMovieResponse> movies) {
        super("multiple.movies.found.title", "multiple.movies.found.detail", HttpStatus.CONFLICT,
                Map.of("options", movies, "errorCode", ApiErrorCode.MULTIPLE_MOVIES_FOUND.getCode()));
    }

}