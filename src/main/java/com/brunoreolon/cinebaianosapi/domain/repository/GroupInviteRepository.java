package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.GroupInvite;
import com.brunoreolon.cinebaianosapi.domain.model.GroupInviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupInviteRepository extends JpaRepository<GroupInvite, Long> {

    Optional<GroupInvite> findByToken(String token);

    Optional<GroupInvite> findByIdAndGroupId(Long inviteId, Long groupId);

    List<GroupInvite> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, GroupInviteStatus status);

    boolean existsByGroupIdAndInvitedUserIdAndStatus(Long groupId, Long invitedUserId, GroupInviteStatus status);

}