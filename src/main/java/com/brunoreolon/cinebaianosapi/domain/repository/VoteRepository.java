package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, VoteId> {

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.vote = :voteType AND v.movie.chooser = :user")
    int countAllByVoteTypeAndReceiver(@Param("voteType") VoteType voteType, @Param("user") User user);

}
