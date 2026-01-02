package com.brunoreolon.cinebaianosapi.core.resend;

import com.resend.Resend;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
@RequiredArgsConstructor
public class ResendConfig {

    private final ResendProperties resendProperties;

    @Bean
    public Resend resend() {
        return new Resend(resendProperties.getKey());
    }

}
