package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatsRepository extends JpaRepository<User, String> {

    @Query("""
            SELECT
                (SELECT COUNT(m) FROM Movie m WHERE m.chooser = u) AS totalMoviesAdded,
                (SELECT COUNT(v) FROM Vote v WHERE v.voter = u) AS totalVotesGiven,
                (SELECT COUNT(v) FROM Vote v JOIN v.movie mv WHERE mv.chooser = u) AS totalVotesReceived,
                (SELECT COUNT(m) FROM Movie m WHERE m NOT IN (SELECT v.movie FROM Vote v WHERE v.voter = u)) AS moviesPendingVote
            FROM User u
            WHERE u.discordId = :discordId
        """)
    UserSummaryProjection findUserSummaryByDiscordId(@Param("discordId") String discordId);

}