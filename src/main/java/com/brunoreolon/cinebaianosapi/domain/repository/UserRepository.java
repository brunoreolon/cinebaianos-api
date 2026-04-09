package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

//    Optional<User> findByDiscordId(String discordId);

    Optional<User> findByEmail(String email);

    @Query("SELECT m FROM User u JOIN u.movies m WHERE u.id = :userId")
    List<Movie> findAllMoviesById(@Param("userId") Long userId);

    @Query("""
                SELECT u 
                FROM User u 
                LEFT JOIN FETCH u.movies m
                LEFT JOIN FETCH m.genres
                WHERE u.id = :userId
            """)
    Optional<User> findByIdWithMovies(@Param("userId") Long userId);

    boolean existsByDiscordId(String discordId);

    @Modifying
    @Query("""
            update User u
            set u.bannedAt = null,
                u.bannedBy = null,
                u.banReason = null,
                u.expiresAt = null
            where u.bannedAt is not null
              and u.expiresAt is not null
              and u.expiresAt <= :now
            """)
    int clearExpiredBans(@Param("now") LocalDateTime now);

}