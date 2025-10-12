package com.brunoreolon.cinebaianosapi.client;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tmdb")
@NoArgsConstructor
@Getter
@Setter
public class TmdbProperties {

    private String apiKey;
    private String baseUrl;
    private String posterPath;

}
