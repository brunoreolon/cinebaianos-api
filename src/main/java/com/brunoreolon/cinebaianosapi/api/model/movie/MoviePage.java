package com.brunoreolon.cinebaianosapi.api.model.movie;

import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MoviePage {

    private int page;
    private int size;
    private int currentPageElements;
    private long totalElements;
    private long totalPages;
    private List<MovieWithChooserResponse> movies;

}
