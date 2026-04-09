package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.GroupJoinRequest;
import com.brunoreolon.cinebaianosapi.domain.model.GroupJoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupJoinRequestRepository extends JpaRepository<GroupJoinRequest, Long> {

    Optional<GroupJoinRequest> findByIdAndGroupId(Long requestId, Long groupId);

    boolean existsByGroupIdAndUserIdAndStatus(Long groupId, Long userId, GroupJoinRequestStatus status);

    Optional<GroupJoinRequest> findFirstByGroupIdAndUserIdAndStatusOrderByCreatedAtDesc(Long groupId, Long userId, GroupJoinRequestStatus status);

    List<GroupJoinRequest> findByGroupIdAndStatusOrderByCreatedAtDesc(Long groupId, GroupJoinRequestStatus status);

}