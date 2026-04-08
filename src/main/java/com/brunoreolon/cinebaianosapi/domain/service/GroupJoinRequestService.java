package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.domain.event.GroupJoinRequestCreatedEvent;
import com.brunoreolon.cinebaianosapi.domain.event.GroupJoinRequestReviewedEvent;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.domain.exception.GroupConflictException;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.repository.GroupJoinRequestRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class GroupJoinRequestService {

    private final GroupJoinRequestRepository groupJoinRequestRepository;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final UserRegistratioService userRegistratioService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public GroupMember joinOpenGroup(Long groupId, Long userId) {
        Group group = groupService.getById(groupId);

        if (group.getJoinPolicy() != JoinPolicy.OPEN) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.join.policy.not.open.message",
                    new Object[]{groupId},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        if (groupMemberService.isMember(groupId, userId)) {
            throw new GroupConflictException("group.member.already.exists.message", new Object[]{groupId, userId});
        }

        return groupMemberService.addMember(groupId, userId, GroupMemberRole.MEMBER);
    }

    @Transactional
    public GroupJoinRequest createJoinRequest(Long groupId, Long userId) {
        Group group = groupService.getById(groupId);
        User user = userRegistratioService.get(userId);

        if (group.getJoinPolicy() != JoinPolicy.REQUEST) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.join.policy.not.request.message",
                    new Object[]{groupId},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        if (groupMemberService.isMember(groupId, userId)) {
            throw new GroupConflictException("group.member.already.exists.message", new Object[]{groupId, userId});
        }

        if (groupMemberService.isBanned(groupId, userId)) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.member.banned.message",
                    new Object[]{userId, groupId},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        boolean hasPendingRequest = groupJoinRequestRepository
                .existsByGroupIdAndUserIdAndStatus(groupId, userId, GroupJoinRequestStatus.PENDING);
        if (hasPendingRequest) {
            throw new GroupConflictException("group.join.request.pending.already.exists.message", new Object[]{groupId, userId});
        }

        GroupJoinRequest request = GroupJoinRequest.builder()
                .group(group)
                .user(user)
                .status(GroupJoinRequestStatus.PENDING)
                .build();

        GroupJoinRequest savedRequest = groupJoinRequestRepository.save(request);

        List<User> recipients = getManagersRecipients(groupId);
        if (!recipients.isEmpty()) {
            publisher.publishEvent(new GroupJoinRequestCreatedEvent(savedRequest, recipients));
        }

        return savedRequest;
    }

    public List<GroupJoinRequest> getPendingRequestsByGroup(Long groupId) {
        return groupJoinRequestRepository.findByGroupIdAndStatusOrderByCreatedAtDesc(groupId, GroupJoinRequestStatus.PENDING);
    }

    @Transactional
    public GroupJoinRequest approve(Long groupId, Long requestId, Long reviewerId) {
        GroupJoinRequest request = getByGroup(groupId, requestId);

        if (!request.isPending()) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.join.request.invalid.status.message",
                    new Object[]{request.getStatus()},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        if (!groupMemberService.isMember(groupId, request.getUser().getId())) {
            groupMemberService.addMember(groupId, request.getUser().getId(), GroupMemberRole.MEMBER);
        }

        User reviewer = userRegistratioService.get(reviewerId);
        request.approve(reviewer);

        publisher.publishEvent(new GroupJoinRequestReviewedEvent(request, true));

        return request;
    }

    @Transactional
    public GroupJoinRequest reject(Long groupId, Long requestId, Long reviewerId) {
        GroupJoinRequest request = getByGroup(groupId, requestId);

        if (!request.isPending()) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.join.request.invalid.status.message",
                    new Object[]{request.getStatus()},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        User reviewer = userRegistratioService.get(reviewerId);
        request.reject(reviewer);

        publisher.publishEvent(new GroupJoinRequestReviewedEvent(request, false));

        return request;
    }

    private GroupJoinRequest getByGroup(Long groupId, Long requestId) {
        return groupJoinRequestRepository.findByIdAndGroupId(requestId, groupId)
                .orElseThrow(() -> new BusinessException(
                        "entity.not.found.title",
                        "group.join.request.not.found.message",
                        new Object[]{requestId, groupId},
                        HttpStatus.NOT_FOUND
                ));
    }

    private List<User> getManagersRecipients(Long groupId) {
        return groupMemberService.getActiveMembers(groupId).stream()
                .filter(GroupMember::canManage)
                .map(GroupMember::getMember)
                .distinct()
                .toList();
    }

}