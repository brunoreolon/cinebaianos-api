package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.exception.MovieAlreadyRegisteredException;
import com.brunoreolon.cinebaianosapi.domain.exception.MovieNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
import com.brunoreolon.cinebaianosapi.domain.repository.MovieRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final UserRegistratioService userRegistratioService;
    private final VoteService voteService;

    @Transactional
    public Movie save(Movie movie, String chooserID, Long vote) {
        boolean tmdbIdAlreadyExists = movieRepository.findByTmbdId(movie.getTmbdId())
                .filter(m -> !m.equals(movie))
                .isPresent();

        if (tmdbIdAlreadyExists)
            throw new MovieAlreadyRegisteredException(String.format("there is already a movie registered with the tmdb id '%s'",
                    movie.getTmbdId()));

        User chooser = userRegistratioService.get(chooserID);
        movie.setChooser(chooser);

        Movie newMovie = movieRepository.save(movie);

        if (vote != null) {
            Vote newVote = voteService.register(chooser, movie, vote);
            newMovie.getVotes().add(newVote);
        }

        return newMovie;
    }

    public Movie get(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new MovieNotFoundException(String.format("Movie with id '%d' not found",  movieId)));
    }

    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    public void delete(Long movieId) {
        Movie movie = get(movieId);
        movieRepository.delete(movie);
    }

}
