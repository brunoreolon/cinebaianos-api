package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.TmdbConverter;
import com.brunoreolon.cinebaianosapi.api.model.tmdb.TmdbMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.api.model.tmdb.TmdbMovieResponse;
import com.brunoreolon.cinebaianosapi.client.TmdbProperties;
import com.brunoreolon.cinebaianosapi.client.model.ClientMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.client.model.ClientResultsResponse;
import com.brunoreolon.cinebaianosapi.domain.service.TmdbService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tmdb")
@AllArgsConstructor
public class TmdbController {

    private final TmdbService tmdbService;
    private final TmdbConverter tmdbConverter;
    private final TmdbProperties tmdbProperties;

    @GetMapping("/search/movies")
    public ResponseEntity<List<TmdbMovieResponse>> search(@RequestParam(name = "title", required = true) String title,
                                                          @RequestParam(name = "year", required = false) String year,
                                                          @RequestParam(name = "language", required = false) String language) {
        if (language == null) {
            language = tmdbProperties.getLanguage();
        }

        ClientResultsResponse response = tmdbService.search(title, year, language);
        return ResponseEntity.ok().body(tmdbConverter.toMovieResponseList(response.getResults()));
    }

    @GetMapping("/movies/{movieId}")
    public ResponseEntity<TmdbMovieDetailsResponse> searchDetails(@PathVariable("movieId") Long movieId,
                                                                  @RequestParam(name = "language", required = false) String language) {
        if (language == null) {
            language = tmdbProperties.getLanguage();
        }

        ClientMovieDetailsResponse movieDetails = tmdbService.getMovieDetails(movieId, language);
        return ResponseEntity.ok().body(tmdbConverter.toMovieDetailsResponse(movieDetails));
    }

}
