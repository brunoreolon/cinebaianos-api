package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.client.TmdbClient;
import com.brunoreolon.cinebaianosapi.client.TmdbProperties;
import com.brunoreolon.cinebaianosapi.client.exception.ClientException;
import com.brunoreolon.cinebaianosapi.client.model.ClientMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.client.model.ClientResultsResponse;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TmdbService {

    private final TmdbProperties tmdbProperties;
    private final TmdbClient tmdbClient;

    public ClientResultsResponse search(String title, String year, String language) {
        try {
            ClientResultsResponse response = tmdbClient.getMovie(tmdbProperties.getApiKey(), title, year, language);

            if (response == null || response.getResults().isEmpty())
                throw new ClientException(
                        String.format("Movie with title '%s' not found", title),
                        HttpStatus.NOT_FOUND,
                        ApiErrorCode.MOVIE_NOT_FOUND);

            return response;
        } catch (FeignException e) {
            handleFeignException(e);
            return null;
        }
    }

    public ClientMovieDetailsResponse getMovieDetails(Long movieId, String language) {
        try {
            return tmdbClient.getMovieDetails(movieId, tmdbProperties.getApiKey(), language, "credits");
        } catch (FeignException e) {
            if (e instanceof FeignException.NotFound) {
                throw new ClientException(
                        String.format("Movie with id '%s' not found", movieId),
                        HttpStatus.NOT_FOUND,
                        ApiErrorCode.MOVIE_NOT_FOUND);
            }

            handleFeignException(e);
            return null;
        } catch (Exception e) {
            throw new ClientException(
                    "Unexpected error getting movie details",
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    ApiErrorCode.UNEXPECTED_ERROR);
        }
    }

    private void handleFeignException(FeignException e) {
        if (e instanceof FeignException.Unauthorized) {
            throw new ClientException(
                    "Invalid or unauthorized API key",
                    HttpStatus.UNAUTHORIZED,
                    ApiErrorCode.TMDB_API_UNAUTHORIZED);
        }
        if (e instanceof FeignException.BadRequest) {
            throw new ClientException(
                    "Invalid request to the TMDb API",
                    HttpStatus.BAD_REQUEST,
                    ApiErrorCode.TMDB_API_BAD_REQUEST);
        }

        throw new ClientException(
                "Error communicating with the TMDb API",
                HttpStatus.SERVICE_UNAVAILABLE,
                ApiErrorCode.TMDB_API_COMMUNICATION_ERROR);
    }

}
