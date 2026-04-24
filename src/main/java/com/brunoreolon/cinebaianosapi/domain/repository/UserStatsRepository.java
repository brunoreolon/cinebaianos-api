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
                (SELECT COUNT(gm) FROM GroupMovie gm WHERE gm.chooser = u AND gm.group.id = :groupId) AS totalMoviesAdded,
                (SELECT COUNT(v) FROM Vote v WHERE v.voter = u AND v.groupMovie.group.id = :groupId) AS totalVotesGiven,
                (SELECT COUNT(v) FROM Vote v WHERE v.groupMovie.chooser = u AND v.groupMovie.group.id = :groupId) AS totalVotesReceived,
                (SELECT COUNT(gm) FROM GroupMovie gm WHERE gm.group.id = :groupId AND gm NOT IN (SELECT v.groupMovie FROM Vote v WHERE v.voter = u AND v.groupMovie.group.id = :groupId)) AS moviesPendingVote
            FROM User u
            WHERE u.id = :userId
        """)
    UserSummaryProjection findUserSummaryByIdAndGroup(@Param("userId") Long userId, @Param("groupId") Long groupId);

}