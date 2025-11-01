package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByTmdbId(String tmdbId);

    @Query("""
                SELECT m.genre AS genre, COUNT(m) AS count 
                FROM Movie m
                GROUP BY m.genre
                ORDER BY m.genre
            """)
    List<GenreCountProjection> findGenreCountsProjections();

    @Query("""
                SELECT m.genre AS genre, v.vote.name AS voteType, COUNT(v) AS total
                FROM Vote v
                JOIN v.movie m
                WHERE (:voteTypeId IS NULL OR v.vote.id = :voteTypeId)
                GROUP BY m.genre, v.vote.name
            """)
    List<GenreVoteTypeCountProjection> findGenreVoteTypeCountProjection(@Param("voteTypeId") Long voteTypeId);

    @Query("SELECT DISTINCT m.genre FROM Movie m ORDER BY m.genre")
    List<String> findAllGenres();

    @Query("SELECT m.genre FROM Movie m WHERE m.chooser.discordId = :discordId")
    List<String> findGenresByChooserDiscordId(@Param("discordId") String discordId);

}
