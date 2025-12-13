package com.brunoreolon.cinebaianosapi.core.security.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public enum AuthErrorReason {

    MISSING_TOKEN("Missing Token", "The access token is missing from the request", "token_missing"),
    BLACKLISTED("Token Blacklisted", "The provided token has been revoked", "token_blacklisted"),
    EXPIRED("Token Expired", "The access token has expired", "token_expired"),
    INVALID("Invalid Token", "The access token is invalid", "token_invalid");

    private final String title;
    private final String defaultDetail;
    private final String code;

    public Map<String, Object> getCodeAsMap() {
        return Map.of("errorCode", this.code);
    }

}
