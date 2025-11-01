package com.brunoreolon.cinebaianosapi.util;

import com.brunoreolon.cinebaianosapi.client.TmdbProperties;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class PosterPathUtil {

    private final TmdbProperties tmdbProperties;

    public String fullPosterPath(String path) {
        return (path != null) ? tmdbProperties.getPosterPath() + path : null;
    }

}
