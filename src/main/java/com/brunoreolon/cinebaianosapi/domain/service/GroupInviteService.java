package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.domain.event.GroupDirectInviteCreatedEvent;
import com.brunoreolon.cinebaianosapi.domain.event.GroupInviteAcceptedEvent;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.domain.exception.GroupConflictException;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.repository.GroupInviteRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class GroupInviteService {

    private final GroupInviteRepository groupInviteRepository;
    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final UserRegistratioService userRegistratioService;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public GroupInvite createInvite(Long groupId, Long createdById, Long invitedUserId, Integer maxUses, LocalDateTime expiresAt) {
        Group group = groupService.getById(groupId);
        User createdBy = userRegistratioService.get(createdById);

        User invitedUser = null;
        GroupInviteType inviteType = GroupInviteType.GENERIC;

        if (invitedUserId != null) {
            invitedUser = userRegistratioService.get(invitedUserId);
            inviteType = GroupInviteType.DIRECT;

            if (groupMemberService.isMember(groupId, invitedUserId)) {
                throw new GroupConflictException("group.member.already.exists.message", new Object[]{groupId, invitedUserId});
            }

            boolean hasPendingDirectInvite = groupInviteRepository
                    .existsByGroupIdAndInvitedUserIdAndStatus(groupId, invitedUserId, GroupInviteStatus.PENDING);
            if (hasPendingDirectInvite) {
                throw new GroupConflictException("group.invite.pending.already.exists.message", new Object[]{groupId, invitedUserId});
            }
        }

        int inviteMaxUses = maxUses == null ? group.getInviteMaxUses() : maxUses;
        validateInviteLimits(inviteMaxUses, group.getInviteMaxUses());

        GroupInvite invite = GroupInvite.builder()
                .group(group)
                .createdBy(createdBy)
                .invitedUser(invitedUser)
                .token(UUID.randomUUID().toString())
                .inviteType(inviteType)
                .status(GroupInviteStatus.PENDING)
                .maxUses(inviteMaxUses)
                .usesCount(0)
                .expiresAt(expiresAt)
                .build();

        GroupInvite savedInvite = groupInviteRepository.save(invite);

        if (savedInvite.getInviteType() == GroupInviteType.DIRECT && savedInvite.getInvitedUser() != null) {
            publisher.publishEvent(new GroupDirectInviteCreatedEvent(savedInvite));
        }

        return savedInvite;
    }

    public List<GroupInvite> getPendingInvitesByGroup(Long groupId) {
        return groupInviteRepository.findByGroupIdAndStatusOrderByCreatedAtDesc(groupId, GroupInviteStatus.PENDING);
    }

    @Transactional
    public void revokeInvite(Long groupId, Long inviteId) {
        GroupInvite invite = getInviteByGroup(inviteId, groupId);

        if (!invite.isPending()) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.invite.invalid.status.message",
                    new Object[]{invite.getStatus()},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        invite.revoke();
    }

    @Transactional
    public GroupMember acceptInvite(String token, Long userId) {
        GroupInvite invite = groupInviteRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException(
                        "entity.not.found.title",
                        "group.invite.not.found.message",
                        new Object[]{token},
                        HttpStatus.NOT_FOUND
                ));

        if (!invite.isPending()) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.invite.invalid.status.message",
                    new Object[]{invite.getStatus()},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        if (invite.isExpired()) {
            invite.expire();
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.invite.expired.message",
                    new Object[]{invite.getId()},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        if (!invite.hasRemainingUses()) {
            invite.expire();
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.invite.max.uses.reached.message",
                    new Object[]{invite.getId()},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        if (!invite.canBeUsedBy(userId)) {
            throw new BusinessException(
                    "access.denied.title",
                    "group.invite.user.not.allowed.message",
                    new Object[]{invite.getId()},
                    HttpStatus.FORBIDDEN
            );
        }

        if (groupMemberService.isMember(invite.getGroup().getId(), userId)) {
            throw new GroupConflictException("group.member.already.exists.message", new Object[]{invite.getGroup().getId(), userId});
        }

        GroupMember member = groupMemberService.addMember(invite.getGroup().getId(), userId, GroupMemberRole.MEMBER);
        invite.consumeUse();

        User acceptedBy = userRegistratioService.get(userId);
        publisher.publishEvent(new GroupInviteAcceptedEvent(invite, acceptedBy));

        return member;
    }

    private GroupInvite getInviteByGroup(Long inviteId, Long groupId) {
        return groupInviteRepository.findByIdAndGroupId(inviteId, groupId)
                .orElseThrow(() -> new BusinessException(
                        "entity.not.found.title",
                        "group.invite.not.found.by.group.message",
                        new Object[]{inviteId, groupId},
                        HttpStatus.NOT_FOUND
                ));
    }

    private void validateInviteLimits(Integer inviteMaxUses, Integer groupMaxUses) {
        if (inviteMaxUses == null || inviteMaxUses < 1) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.invite.max.uses.invalid.message",
                    new Object[]{inviteMaxUses},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        if (groupMaxUses > 0 && inviteMaxUses > groupMaxUses) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.invite.max.uses.exceeded.group.limit.message",
                    new Object[]{inviteMaxUses, groupMaxUses},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }
    }

}