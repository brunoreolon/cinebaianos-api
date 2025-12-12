package com.brunoreolon.cinebaianosapi.core.security;

import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;

import java.util.Map;

public enum AuthErrorReason {

    MISSING_TOKEN("Missing Token", "The access token is missing from the request", ApiErrorCode.MISSING_ACCESS_TOKEN),
    BLACKLISTED("Token Blacklisted", "The provided token has been revoked", ApiErrorCode.BLACKLISTED_ACCESS_TOKEN),
    EXPIRED("Token Expired", "The access token has expired", ApiErrorCode.EXPIRED_ACCESS_TOKEN),
    INVALID("Invalid Token", "The access token is invalid", ApiErrorCode.INVALID_ACCESS_TOKEN);

    private final String title;
    private final String detail;
    private final ApiErrorCode code;

    AuthErrorReason(String title, String detail, ApiErrorCode code) {
        this.title = title;
        this.detail = detail;
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public String getDetail() {
        return detail;
    }

    public ApiErrorCode getCode() {
        return code;
    }

    public Map<String, Object> getCodeAsMap() {
        return Map.of("errorCode", this.code.getCode());
    }

}
