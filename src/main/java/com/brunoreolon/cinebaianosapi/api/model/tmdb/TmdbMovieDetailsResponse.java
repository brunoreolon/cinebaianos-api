package com.brunoreolon.cinebaianosapi.api.model.tmdb;

import com.brunoreolon.cinebaianosapi.client.model.GenreResponse;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TmdbMovieDetailsResponse {

    private Long id;
    private List<GenreResponse> genres;
    private String title;
    private String overview;
    private LocalDate releaseDate;
    private String posterPath;

}
