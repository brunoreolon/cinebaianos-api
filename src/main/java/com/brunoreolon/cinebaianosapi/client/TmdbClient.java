package com.brunoreolon.cinebaianosapi.client;

import com.brunoreolon.cinebaianosapi.client.model.ClientMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.client.model.ClientResultsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "TmdbClient",
        url = "${tmdb.base-url}"
)
public interface TmdbClient {

    @GetMapping("/search/movie")
    ClientResultsResponse getMovie(@RequestParam("api_key") String apiKey,
                                   @RequestParam("query") String title,
                                   @RequestParam("year") String year,
                                   @RequestParam("language") String language);

    @GetMapping("/movie/{movieId}")
    ClientMovieDetailsResponse getMovieDetails(@PathVariable(name = "movieId") Long movieId,
                                               @RequestParam("api_key") String apiKey,
                                               @RequestParam("language") String language);

}
