package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, VoteId> {

    @Query("SELECT v FROM Vote v JOIN FETCH v.movie WHERE v.id = :id")
    Optional<Vote> findByIdWithMovie(@Param("id") VoteId id);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.vote = :voteType AND v.movie.chooser = :user")
    Long countAllByVoteTypeAndReceiver(@Param("voteType") VoteType voteType, @Param("user") User user);

    @Query("""
                SELECT v
                FROM Vote v
                JOIN FETCH v.movie
                JOIN FETCH v.voter
                JOIN FETCH v.vote
                WHERE v.voter.discordId = :discordId
            """)
    List<Vote> findByVoterWithMovie(@Param("discordId") String discordId);

    @Query("""
                SELECT v
                FROM Vote v
                JOIN FETCH v.voter u
                JOIN FETCH v.vote vt
                JOIN FETCH v.movie m
                WHERE m.id = :movieId
            """)
    List<Vote> findByMovieId(@Param("movieId") Long movieId);
}
