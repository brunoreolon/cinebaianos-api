package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByDiscordId(String discordId);

    Optional<User> findByEmail(String email);

    @Query("""
                SELECT u 
                FROM User u 
                LEFT JOIN FETCH u.movies m
                LEFT JOIN FETCH m.genres
                WHERE u.id = :id
            """)
    Optional<User> findByIdWithMovies(@Param("discordId") Long id);

}