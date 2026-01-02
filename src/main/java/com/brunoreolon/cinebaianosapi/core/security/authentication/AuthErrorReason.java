package com.brunoreolon.cinebaianosapi.core.security.authentication;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public enum AuthErrorReason {

    MISSING_TOKEN("auth.token_missing.title", "auth.token_missing.message", "token_missing"),
    BLACKLISTED("auth.token_blacklisted.title", "auth.token_blacklisted.message", "token_blacklisted"),
    EXPIRED("auth.token_expired.title", "auth.token_expired.message", "token_expired"),
    INVALID("auth.token_invalid.title", "auth.token_invalid.message", "token_invalid");

    private final String titleKey;
    private final String messageKey;
    private final String code;

    public Map<String, Object> getCodeAsMap() {
        return Map.of("errorCode", this.code);
    }

}