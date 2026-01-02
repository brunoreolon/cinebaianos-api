package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.client.TmdbClient;
import com.brunoreolon.cinebaianosapi.client.TmdbProperties;
import com.brunoreolon.cinebaianosapi.client.exception.ClientException;
import com.brunoreolon.cinebaianosapi.client.model.ClientMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.client.model.CrewResponse;
import com.brunoreolon.cinebaianosapi.domain.model.Genre;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.repository.GenreRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.MovieRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MovieUpdateService {

    private static final Logger log = LoggerFactory.getLogger(MovieUpdateService.class);

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final TmdbClient tmdbClient;
    private final TmdbProperties tmdbProperties;

    private String getDirector(ClientMovieDetailsResponse movieDetails) {
        return movieDetails.getCredits().getCrew().stream()
                .filter(c -> "Director".equalsIgnoreCase(c.getJob()))
                .map(CrewResponse::getName)
                .findFirst()
                .orElse("Desconhecido");
    }

    /**
     * Atualiza todos os filmes da base de dados.
     */
    @Transactional
    public void updateMoviesFromTmdb() {
        List<Movie> movies = movieRepository.findAll();
        log.info("Início da atualização de filmes: total de {} filmes", movies.size());

        int updatedCount = 0;
        int failedCount = 0;

        for (int i = 0; i < movies.size(); i++) {
            Movie movie = movies.get(i);

            boolean success = updateSingleMovie(movie);
            if (success) updatedCount++;
            else failedCount++;

            try {
                Thread.sleep(200); // pausa para não sobrecarregar a API
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
                log.warn("Thread de atualização interrompida durante pausa");
            }
        }

        log.info("Atualização finalizada: {} filmes atualizados com sucesso, {} falharam", updatedCount, failedCount);
    }

    /**
     * Atualiza um único filme.
     *
     * @param movie filme a ser atualizado
     * @return true se atualizado com sucesso, false caso tenha ocorrido algum erro
     */
    @Transactional
    public boolean updateSingleMovie(Movie movie) {
        if (movie.getTmdbId() == null) {
            log.debug("Filme '{}' ignorado: TMDB ID ausente", movie.getTitle());
            return false;
        }

        try {
            ClientMovieDetailsResponse details = tmdbClient.getMovieDetails(
                    Long.parseLong(movie.getTmdbId()),
                    tmdbProperties.getApiKey(),
                    "pt-BR",
                    "credits"
            );

            // Atualiza informações básicas
            movie.setSynopsis(details.getSynopsis());
            movie.setDirector(getDirector(details));
            movie.setDuration(details.getDuration());

            // Atualiza gêneros
            Set<Genre> genres = details.getGenres().stream()
                    .map(genreResp -> genreRepository.findByName(genreResp.getName())
                            .orElseGet(() -> {
                                Genre g = new Genre(genreResp.getId(), genreResp.getName());
                                Genre saved = genreRepository.save(g);
                                log.debug("Novo gênero criado: {}", g.getName());
                                return saved;
                            }))
                    .collect(Collectors.toSet());

            movie.setGenres(genres);
            for (Genre g : genres) {
                if (!g.getMovies().contains(movie)) {
                    g.getMovies().add(movie);
                }
            }

            movieRepository.save(movie);

            log.info("Filme atualizado com sucesso: '{}' | Diretor: '{}' | Duração: {} | Gêneros: {}",
                    movie.getTitle(),
                    movie.getDirector(),
                    movie.getDuration(),
                    genres.stream().map(Genre::getName).collect(Collectors.joining(", "))
            );

            return true;

        } catch (ClientException e) {
            log.warn("Erro ao buscar detalhes TMDb para '{}': {}", movie.getTitle(), e.getMessage());
        } catch (DataAccessException e) {
            log.error("Erro ao salvar filme '{} - {}' no banco: {}", movie.getTmdbId(), movie.getTitle(), e.getMessage(), e);
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar filme '{} - {}': {}", movie.getTmdbId(), movie.getTitle(), e.getMessage(), e);
        }

        return false;
    }

}