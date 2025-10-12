package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByDiscordId(String discordId);

    Optional<User> findByEmail(String email);

    @Query("SELECT m FROM User u JOIN u.movies m WHERE u.discordId = :discordId")
    List<Movie> findAllMoviesByDiscordId(@Param("discordId")  String discordId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.movies WHERE u.discordId = :discordId")
    Optional<User> findByDiscordIdWithMovies(@Param("discordId") String discordId);

}
