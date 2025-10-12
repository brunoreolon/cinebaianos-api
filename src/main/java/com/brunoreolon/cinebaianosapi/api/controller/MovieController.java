package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.MovieConverter;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieVoteDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.request.MovieIdRequest;
import com.brunoreolon.cinebaianosapi.api.model.movie.request.MovieSearchRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.id.VoteTypeId;
import com.brunoreolon.cinebaianosapi.client.converter.TmdbConverter;
import com.brunoreolon.cinebaianosapi.client.model.MovieResponse;
import com.brunoreolon.cinebaianosapi.client.model.TmdbMovieResponse;
import com.brunoreolon.cinebaianosapi.client.model.TmdbResponse;
import com.brunoreolon.cinebaianosapi.domain.exception.MultipleMoviesFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.service.MovieService;
import com.brunoreolon.cinebaianosapi.domain.service.TmdbService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/movies")
@AllArgsConstructor
public class MovieController {

    public static final String LANGUAGE = "pt-BR";
    private final MovieService movieService;
    private final TmdbService tmdbService;
    private final MovieConverter movieConverter;
    private final TmdbConverter tmdbConverter;

    @PostMapping
    public ResponseEntity<MovieVoteDetailResponse> addById(@Valid @RequestBody MovieIdRequest movieIdRequest,
                                                           @RequestParam(name = "language", defaultValue = LANGUAGE) String language) {
        MovieResponse movieResponse = tmdbConverter.convertMovieDetail(
                tmdbService.getMovieDetails(movieIdRequest.getMovie().getId(), language));
        Movie movie = tmdbConverter.toEntity(movieResponse);
        Long voteId = getVoteId(movieIdRequest.getVote());
        Movie newMovie = movieService.save(movie, movieIdRequest.getChooser().getDiscordId(), voteId);

        return ResponseEntity.status(HttpStatus.CREATED).body(movieConverter.toMovieVoteDetailResponse(newMovie, movie.getChooser().getDiscordId()));
    }

    @PostMapping("/candidates")
    public ResponseEntity<MovieVoteDetailResponse> searchAndAddMovie(@Valid @RequestBody MovieSearchRequest movieSearchRequest,
                                                                     @RequestParam(name = "language", defaultValue = LANGUAGE) String language) {
        TmdbResponse response = tmdbService.getMovie(movieSearchRequest.getTitle(), movieSearchRequest.getYear(), language);
        List<TmdbMovieResponse> tmdbMovieResponses = tmdbConverter.converteList(response.getResults());

        if (tmdbMovieResponses.size() > 1)
            throw new MultipleMoviesFoundException(tmdbMovieResponses);

        MovieResponse movieResponse = tmdbConverter.convertMovieDetail(
                tmdbService.getMovieDetails(tmdbMovieResponses.getFirst().getId(), language));

        Movie movie = tmdbConverter.toEntity(movieResponse);

        Long voteId = getVoteId(movieSearchRequest.getVote());

        Movie newMovie = movieService.save(movie, movieSearchRequest.getChooser().getDiscordId(), voteId);
        MovieVoteDetailResponse movieVote = movieConverter.toMovieVoteDetailResponse(newMovie, newMovie.getChooser().getDiscordId());

        return ResponseEntity.status(HttpStatus.CREATED).body(movieVote);
    }

    private static Long getVoteId(Optional<VoteTypeId> voteType) {
        return voteType.map(VoteTypeId::getId).orElse(null);
    }

    @GetMapping
    public ResponseEntity<List<MovieWithChooserResponse>> getAll() {
        List<MovieWithChooserResponse> collectionModel = movieConverter.toWithChooserResponseList(movieService.getAll());
        return ResponseEntity.ok().body(collectionModel);
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<MovieDetailResponse> get(@PathVariable("movieId") Long movieId) {
        Movie movie = movieService.get(movieId);
        return ResponseEntity.ok().body(movieConverter.toDetailResponse(movie));
    }

    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> delete(@PathVariable("movieId") Long movieId) {
        movieService.delete(movieId);
        return ResponseEntity.noContent().build();
    }

}
