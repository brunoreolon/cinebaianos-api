package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long>, JpaSpecificationExecutor<Movie> {

    Optional<Movie> findByTmdbId(String tmdbId);

    @Query("SELECT m FROM Movie m JOIN FETCH m.genres WHERE m.id = :id")
    Optional<Movie> findByIdWithGenres(@Param("id") Long id);

    @Query("""
                SELECT g.name AS genre, COUNT(m) AS count
                FROM Movie m
                JOIN m.genres g
                WHERE g.name IS NOT NULL
                GROUP BY g.name
                ORDER BY COUNT(m) DESC
            """)
    List<GenreCountProjection> findGenreCountsProjections();

    @Query("""
                SELECT g.name AS genre, v.vote.name AS voteType, COUNT(v) AS total
                FROM Vote v
                JOIN v.movie m
                JOIN m.genres g
                WHERE (:voteTypeId IS NULL OR v.vote.id = :voteTypeId)
                GROUP BY g.name, v.vote.name
            """)
    List<GenreVoteTypeCountProjection> findGenreVoteTypeCountProjection(@Param("voteTypeId") Long voteTypeId);

    @Query("""
            SELECT DISTINCT g.name
            FROM Movie m
            JOIN m.genres g
            ORDER BY g.name
            """)
    List<String> findAllGenres();

    @Query("""
            SELECT g.name
            FROM Movie m
            JOIN m.genres g
            WHERE m.chooser.discordId = :discordId
            """)
    List<String> findGenresByChooserDiscordId(@Param("discordId") String discordId);

}
