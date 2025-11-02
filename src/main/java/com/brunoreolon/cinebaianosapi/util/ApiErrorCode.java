package com.brunoreolon.cinebaianosapi.util;

public enum ApiErrorCode {

    INVALID_OR_EXPIRED_REFRESH_TOKEN("invalid_or_expired_refresh_token"),
    USER_NOT_FOUND("user_not_found"),
    USER_ALREADY_REGISTERED("user_already_registered"),
    MOVIE_NOT_FOUND("movie_not_found"),
    MOVIE_ALREADY_REGISTERED("movie_already_registered"),
    MULTIPLE_MOVIES_FOUND("MULTIPLE_movies_found"),
    INVALID_VOTE("invalid_vote"),
    VOTE_ALREADY_REGISTERED("vote_already_registered"),
    VOTE_INVALID_STATUS("status_vote_invalid"),
    VOTE_TYPE_ALREADY_REGISTERED("vote_type_already_registered"),
    VOTE_TYPE_NOT_FOUND("vote_type_not_found_registered"),
    TMDB_API_COMMUNICATION_ERROR("tmdb_api_communication_error"),
    TMDB_API_BAD_REQUEST("tmdb_api_bad_request"),
    TMDB_API_UNAUTHORIZED("tmdb_api_unauthorized"),
    UNEXPECTED_ERROR("unexpected_error");

    private final String code;

    ApiErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
