package com.brunoreolon.cinebaianosapi.core.resend;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "resend.api")
@Getter
@Setter
public class ResendProperties {

    private String key;
    private String from;

}