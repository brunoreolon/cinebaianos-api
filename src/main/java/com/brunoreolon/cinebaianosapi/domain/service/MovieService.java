package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.domain.exception.MovieAlreadyRegisteredException;
import com.brunoreolon.cinebaianosapi.domain.exception.MovieNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.repository.GenreRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.MovieRepository;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MovieService implements OwnableService<Movie, Long> {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final UserRegistratioService userRegistratioService;
    private final VoteService voteService;

    @Transactional
    public Movie save(Movie movie, String chooserID, Long vote) {
        boolean tmdbIdAlreadyExists = movieRepository.findByTmdbId(movie.getTmdbId())
                .filter(m -> !m.equals(movie))
                .isPresent();

        if (tmdbIdAlreadyExists)
            throw new MovieAlreadyRegisteredException(String.format("there is already a movie registered with the tmdb id '%s'",
                    movie.getTmdbId()));

        User chooser = userRegistratioService.get(chooserID);

        if (chooser.isBot())
            throw new BusinessException(
                    "Bot users cannot have movies added.",
                    HttpStatus.FORBIDDEN,
                    "Action Not Allowed",
                    ApiErrorCode.BOT_USER_FORBIDDEN.asMap());

        movie.setChooser(chooser);
        movie.setGenres(getGenres(movie));

        Movie newMovie = movieRepository.save(movie);

        if (vote != null) {
            Vote newVote = voteService.register(chooser, movie, vote);
            newMovie.getVotes().add(newVote);
        }

        return newMovie;
    }

    private Set<Genre> getGenres(Movie movie) {
        return movie.getGenres().stream()
                .map(g -> genreRepository.findById(g.getId())
                        .orElseGet(() -> {
                            Genre genre = new Genre(g.getId(), g.getName());
                            return genreRepository.save(genre);
                        }))
                .collect(Collectors.toSet());
    }

    @Override
    public Movie get(Long movieId) {
        return movieRepository.findByIdWithGenres(movieId)
                .orElseThrow(() -> new MovieNotFoundException(String.format("Movie with id '%d' not found", movieId)));
    }

    public Page<Movie> getAll(Specification<Movie> specification, Pageable pageable) {
        return movieRepository.findAll(specification, pageable);
    }

    public void delete(Long movieId) {
        Movie movie = get(movieId);
        movieRepository.delete(movie);
    }

}
