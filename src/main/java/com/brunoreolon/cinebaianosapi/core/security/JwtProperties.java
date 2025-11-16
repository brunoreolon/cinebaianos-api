package com.brunoreolon.cinebaianosapi.core.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "security.jwt")
@NoArgsConstructor
@Getter
@Setter
public class JwtProperties {

    private String path;
    private String keystoreBase64;
    private String storepass;
    private String alias;
    private String keypass;
    private long accessTokenExpirationMinutes;
    private long refreshTokenExpirationMinutes;

}
