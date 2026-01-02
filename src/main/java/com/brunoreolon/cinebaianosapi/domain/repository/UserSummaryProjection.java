package com.brunoreolon.cinebaianosapi.domain.repository;

public interface UserSummaryProjection {

    Long getTotalMoviesAdded();
    Long getTotalVotesGiven();
    Long getTotalVotesReceived();
    Long getMoviesPendingVote();

}