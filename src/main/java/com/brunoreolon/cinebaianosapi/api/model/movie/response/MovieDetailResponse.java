package com.brunoreolon.cinebaianosapi.api.model.movie.response;

import com.brunoreolon.cinebaianosapi.api.model.genre.GenreResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MovieDetailResponse {

    private Long id;
    private String title;
//    private String genre;
    private String year;
    private String tmdbId;
    private LocalDateTime dateAdded;
    private String synopsis;
    private String director;
    private List<GenreResponse> genres;
    private String posterPath;

}
