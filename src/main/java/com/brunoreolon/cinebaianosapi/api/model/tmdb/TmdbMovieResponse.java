package com.brunoreolon.cinebaianosapi.api.model.tmdb;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TmdbMovieResponse {

    private Long id;
    private String title;
    private String overview;
    private LocalDate releaseDate;
    private String posterPath;

}
