package com.brunoreolon.cinebaianosapi.domain.repository;

public interface GenreVoteTypeCountProjection {

    String getGenre();
    String getVoteType();
    Long getTotal();

}
