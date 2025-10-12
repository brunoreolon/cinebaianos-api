package com.brunoreolon.cinebaianosapi.domain.exception;

import com.brunoreolon.cinebaianosapi.client.model.TmdbMovieResponse;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class MultipleMoviesFoundException extends BusinessException {

    public static final String MULTIPLE_MOVIES_TITLE = "Multiple Movies Found";
    public static final String MULTIPLE_MOVIES_DETAIL = "More than one movie found. Choose the correct one to add.";

    public MultipleMoviesFoundException(List<TmdbMovieResponse> movies) {
        super(MULTIPLE_MOVIES_DETAIL, HttpStatus.CONFLICT, MULTIPLE_MOVIES_TITLE, Map.of("options", movies));
    }

}
