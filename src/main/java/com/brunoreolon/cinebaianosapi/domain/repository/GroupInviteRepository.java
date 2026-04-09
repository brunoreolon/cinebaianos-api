package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.GroupInvite;
import com.brunoreolon.cinebaianosapi.domain.model.GroupInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long> {

    Optional<GroupInvite> findByToken(String token);

    Optional<GroupInvite> findByIdAndGroupId(Long inviteId, Long groupId);

    List<GroupInvite> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, GroupInviteStatus status);

    @Query("""
            select i
            from GroupInvite i
            where i.invitedUser.id = :userId
              and i.status = :status
              and (i.expiresAt is null or i.expiresAt > :now)
              and i.usesCount < i.maxUses
            order by i.createdAt desc
            """)
    List<GroupInvite> findPendingReceivedInvites(@Param("userId") Long userId,
                                                 @Param("status") GroupInviteStatus status,
                                                 @Param("now") LocalDateTime now);

    boolean existsByGroupIdAndInvitedUserIdAndStatus(Long groupId, Long invitedUserId, GroupInviteStatus status);

}