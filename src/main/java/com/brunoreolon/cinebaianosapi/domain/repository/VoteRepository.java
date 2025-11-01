package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoteRepository extends JpaRepository<Vote, VoteId> {

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.vote = :voteType AND v.movie.chooser = :user")
    Long countAllByVoteTypeAndReceiver(@Param("voteType") VoteType voteType, @Param("user") User user);

    @Query("SELECT v FROM Vote v JOIN FETCH v.movie JOIN FETCH v.voter WHERE v.voter.discordId = :discordId")
    List<Vote> findByVoterWithMovie(@Param("discordId") String discordId);

    List<Vote> findByMovieId(Long movieId);
}
