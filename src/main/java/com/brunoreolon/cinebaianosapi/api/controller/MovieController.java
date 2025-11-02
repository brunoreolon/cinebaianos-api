package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.MovieConverter;
import com.brunoreolon.cinebaianosapi.api.converter.TmdbConverter;
import com.brunoreolon.cinebaianosapi.api.converter.UserConverter;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieVoteDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.request.MovieIdRequest;
import com.brunoreolon.cinebaianosapi.api.model.movie.request.MovieSearchRequest;
import com.brunoreolon.cinebaianosapi.api.model.tmdb.TmdbMovieResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserWithMoviesResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.id.VoteTypeId;
import com.brunoreolon.cinebaianosapi.client.TmdbProperties;
import com.brunoreolon.cinebaianosapi.client.model.ClientMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.client.model.ClientResultsResponse;
import com.brunoreolon.cinebaianosapi.core.security.ApplicationService;
import com.brunoreolon.cinebaianosapi.core.security.CheckSecurity;
import com.brunoreolon.cinebaianosapi.domain.exception.MultipleMoviesFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.service.MovieService;
import com.brunoreolon.cinebaianosapi.domain.service.TmdbService;
import com.brunoreolon.cinebaianosapi.domain.service.UserService;
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

    private final MovieService movieService;
    private final TmdbService tmdbService;
    private final UserService userService;
    private final ApplicationService applicationService;
    private final MovieConverter movieConverter;
    private final TmdbConverter tmdbConverter;
    private final UserConverter userConverter;
    private final TmdbProperties tmdbProperties;

    @PostMapping
    @CheckSecurity.CanAccess
    public ResponseEntity<MovieVoteDetailResponse> addById(@Valid @RequestBody MovieIdRequest movieIdRequest,
                                                           @RequestParam(name = "language", required = false) String language) {
        if (language == null) {
            language = tmdbProperties.getLanguage();
        }

        applicationService.checkCanAddMovieFor(movieIdRequest.getChooser().getDiscordId());

        ClientMovieDetailsResponse movieDetails = tmdbService.getMovieDetails(movieIdRequest.getMovie().getId(), language);
        Movie movie = tmdbConverter.toEntityFromClientMovieDetail(movieDetails);
        Long voteId = getVoteId(movieIdRequest.getVote());
        Movie newMovie = movieService.save(movie, movieIdRequest.getChooser().getDiscordId(), voteId);

        return ResponseEntity.status(HttpStatus.CREATED).body(movieConverter.toMovieVoteDetailResponse(newMovie, movie.getChooser().getDiscordId()));
    }

    @PostMapping("/candidates")
    @CheckSecurity.CanAccess
    public ResponseEntity<MovieVoteDetailResponse> searchAndAddMovie(@Valid @RequestBody MovieSearchRequest movieSearchRequest,
                                                                     @RequestParam(name = "language", required = false) String language) {
        if (language == null) {
            language = tmdbProperties.getLanguage();
        }

        applicationService.checkCanAddMovieFor(movieSearchRequest.getChooser().getDiscordId());

        ClientResultsResponse response = tmdbService.search(movieSearchRequest.getTitle(), movieSearchRequest.getYear(), language);
        List<TmdbMovieResponse> tmdbMovieResponses = tmdbConverter.toMovieResponseList(response.getResults());

        if (tmdbMovieResponses.size() > 1)
            throw new MultipleMoviesFoundException(tmdbMovieResponses);

        ClientMovieDetailsResponse movieDetails = tmdbService.getMovieDetails(tmdbMovieResponses.getFirst().getId(), language);
        Movie movie = tmdbConverter.toEntityFromClientMovieDetail(movieDetails);

        Long voteId = getVoteId(movieSearchRequest.getVote());

        Movie newMovie = movieService.save(movie, movieSearchRequest.getChooser().getDiscordId(), voteId);
        MovieVoteDetailResponse movieVote = movieConverter.toMovieVoteDetailResponse(newMovie, newMovie.getChooser().getDiscordId());

        return ResponseEntity.status(HttpStatus.CREATED).body(movieVote);
    }

    private static Long getVoteId(Optional<VoteTypeId> voteType) {
        return voteType.map(VoteTypeId::getId).orElse(null);
    }

    @GetMapping
    @CheckSecurity.CanAccess
    public ResponseEntity<List<MovieWithChooserResponse>> getAll() {
        List<MovieWithChooserResponse> collectionModel = movieConverter.toWithChooserResponseList(movieService.getAll());
        return ResponseEntity.ok().body(collectionModel);
    }

    @GetMapping("/{movieId}")
    @CheckSecurity.CanAccess
    public ResponseEntity<MovieDetailResponse> get(@PathVariable("movieId") Long movieId) {
        Movie movie = movieService.get(movieId);
        return ResponseEntity.ok().body(movieConverter.toDetailResponse(movie));
    }

    @DeleteMapping("/{movieId}")
    @CheckSecurity.IsOwner(service = "movieService")
    public ResponseEntity<Void> delete(@PathVariable @ResourceId Long movieId) {
        movieService.delete(movieId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{discordId}")
    @CheckSecurity.CanAccess
    public ResponseEntity<UserWithMoviesResponse> getMoviesByUser(@PathVariable String discordId) {
        User user = userService.getWithMovies(discordId);
        UserWithMoviesResponse response = userConverter.toWithMoviesResponse(user);

        return ResponseEntity.ok().body(response);
    }

}
