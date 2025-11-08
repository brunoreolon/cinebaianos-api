package com.brunoreolon.cinebaianosapi.util;

import java.util.Map;

public enum ApiErrorCode {

    INVALID_CREDENTIALS("invalid_credentials"),
    USER_NOT_AUTHORIZED("user_not_authorized"),
    INVALID_OR_EXPIRED_ACCESS_TOKEN("invalid_or_expired_access_token"),
    INVALID_REFRESH_TOKEN("invalid_refresh_token"),
    EXPIRED_REFRESH_TOKEN("expired_refresh_token"),
    USER_NOT_FOUND("user_not_found"),
    USER_ALREADY_REGISTERED("user_already_registered"),
    MOVIE_NOT_FOUND("movie_not_found"),
    MOVIE_ALREADY_REGISTERED("movie_already_registered"),
    MULTIPLE_MOVIES_FOUND("multiple_movies_found"),
    INVALID_VOTE("invalid_vote"),
    VOTE_ALREADY_REGISTERED("vote_already_registered"),
    VOTE_INVALID_STATUS("vote_invalid_status"),
    VOTE_TYPE_ALREADY_REGISTERED("vote_type_already_registered"),
    VOTE_TYPE_NOT_FOUND("vote_type_not_found"),
    TMDB_API_COMMUNICATION_ERROR("tmdb_api_communication_error"),
    TMDB_API_BAD_REQUEST("tmdb_api_bad_request"),
    TMDB_API_UNAUTHORIZED("tmdb_api_unauthorized"),
    UNEXPECTED_ERROR("unexpected_error"),
    BOT_USER_FORBIDDEN("bot_user_forbidden");

    private final String code;

    ApiErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public Map<String, Object> asMap() {
        return Map.of("errorCode", this.code);
    }

}
