package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.GroupInviteStatus;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    @Query("""
            select u
            from User u
            where u.active = true
              and u.isBot = false
              and (u.bannedAt is null or (u.expiresAt is not null and u.expiresAt <= :now))
              and (
                    lower(u.name) like lower(concat('%', :query, '%'))
                 or lower(u.email) like lower(concat('%', :query, '%'))
              )
              and not exists (
                    select gm.member.id
                    from GroupMember gm
                    where gm.group.id = :groupId
                      and gm.member.id = u.id
                      and gm.active = true
              )
              and not exists (
                    select b.member.id
                    from GroupMemberBan b
                    where b.group.id = :groupId
                      and b.member.id = u.id
                      and (b.expiresAt is null or b.expiresAt > :now)
              )
              and not exists (
                    select i.id
                    from GroupInvite i
                    where i.group.id = :groupId
                      and i.invitedUser.id = u.id
                      and i.status = :pendingStatus
                      and (i.expiresAt is null or i.expiresAt > :now)
                      and i.usesCount < i.maxUses
              )
            order by lower(u.name), lower(u.email)
            """)
    Slice<User> searchInviteCandidates(@Param("groupId") Long groupId,
                                       @Param("query") String query,
                                       @Param("pendingStatus") GroupInviteStatus pendingStatus,
                                       @Param("now") LocalDateTime now,
                                       Pageable pageable);

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