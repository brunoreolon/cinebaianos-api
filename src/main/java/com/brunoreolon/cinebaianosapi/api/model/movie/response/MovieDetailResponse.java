package com.brunoreolon.cinebaianosapi.api.model.movie.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MovieDetailResponse {

    private Long id;
    private String title;
    private String genre;
    private String year;
    private String tmdbId;
    private LocalDateTime dateAdded;
    private String posterPath;

}
