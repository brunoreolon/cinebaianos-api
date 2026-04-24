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

    @Query("""
                SELECT v FROM Vote v
                JOIN FETCH v.groupMovie gm
                JOIN FETCH gm.movie
                JOIN FETCH v.voter
                WHERE v.id = :id
            """)
    Optional<Vote> findByIdWithMovieAndVoter(@Param("id") VoteId id);

    @Query("""
                SELECT COUNT(v)
                FROM Vote v
                WHERE v.vote = :voteType
                AND v.groupMovie.movie.chooser = :user
            """)
    Long countAllByVoteTypeAndReceiver(@Param("voteType") VoteType voteType, @Param("user") User user);

    @Query("""
                SELECT COUNT(v)
                FROM Vote v
                WHERE v.vote = :voteType
                AND v.voter = :user
            """)
    Long countAllByVoteTypeAndGiver(@Param("voteType") VoteType voteType, @Param("user") User user);

    @Query("""
                SELECT v
                FROM Vote v
                JOIN FETCH v.groupMovie gm
                JOIN FETCH gm.movie
                JOIN FETCH v.voter
                JOIN FETCH v.vote
                WHERE v.voter.id = :userId
            """)
    List<Vote> findByVoterWithMovie(@Param("userId") Long userId);

    @Query("""
                SELECT v
                FROM Vote v
                JOIN FETCH v.voter u
                JOIN FETCH v.vote vt
                JOIN FETCH v.groupMovie gm
                JOIN FETCH gm.movie m
                WHERE m.id = :movieId
            """)
    List<Vote> findByMovieId(@Param("movieId") Long movieId);

    boolean existsByGroupMovieAndVoterId(GroupMovie groupMovie, Long voterId);

    Optional<Vote> findByGroupMovieAndVoterId(GroupMovie groupMovie, Long voterId);

    @Query("""
        SELECT COUNT(v)
        FROM Vote v
        WHERE v.vote = :voteType
          AND v.groupMovie.movie.chooser = :user
          AND v.groupMovie.group.id = :groupId
    """)
    Long countAllByVoteTypeAndReceiverAndGroup(@Param("voteType") VoteType voteType, @Param("user") User user, @Param("groupId") Long groupId);

}