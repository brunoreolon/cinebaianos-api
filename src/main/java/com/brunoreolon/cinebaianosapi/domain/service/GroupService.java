package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.interfaces.OwnableService;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.domain.exception.GroupConflictException;
import com.brunoreolon.cinebaianosapi.domain.exception.GroupInvalidOperationException;
import com.brunoreolon.cinebaianosapi.domain.exception.GroupNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.repository.GroupRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService implements OwnableService<Group, Long> {

    private final GroupRepository groupRepository;
    private final GroupMemberService groupMemberService;
    private final UserRegistratioService userRegistratioService;

    public GroupService(@Lazy GroupMemberService groupMemberService, GroupRepository groupRepository,
                        UserRegistratioService userRegistratioService) {
        this.groupRepository = groupRepository;
        this.groupMemberService = groupMemberService;
        this.userRegistratioService = userRegistratioService;
    }

    public List<Movie> getMoviesByGroupId(Long groupId) {
        Optional<Group> group = groupRepository.findGroupWithMovies(groupId);

        return group.map(g -> g.getMovies().stream()
                        .map(GroupMovie::getMovie)
                        .toList())
                .orElseThrow(() -> new GroupNotFoundException("group.not.found.message", new Object[]{groupId}));
    }

    public Group getGroupWithMovies(Long groupId) {
        return groupRepository.findGroupWithMovies(groupId)
                .orElseThrow(() -> new GroupNotFoundException("group.not.found.message", new Object[]{groupId}));
    }

    @Transactional
    public Group save(Group group, Long ownerId) {
        validateRequiredFields(group);

        User owner = userRegistratioService.get(ownerId);

        group.setOwner(owner);

        Group newGroup = groupRepository.save(group);

        groupMemberService.addMember(newGroup, owner, GroupMemberRole.OWNER);

        return newGroup;
    }

    @Transactional
    public Group update(Group group) {
        return groupRepository.save(group);
    }

    @Transactional
    public void deleteBySlug(String slug) {
        Group group = getBySlug(slug);
        groupRepository.deleteById(group.getId());
    }

    @Transactional
    public void deleteById(Long groupId) {
        Group group = getById(groupId);
        group.disable();
    }

    public List<Group> getAll() {
        return groupRepository.findAll();
    }

    public List<Group> getAllForAdmin() {
        return groupRepository.findAllForAdmin();
    }

    public Group getById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("group.not.found.message", new Object[]{groupId}));
    }

    public Group getBySlug(String slug) {
        return groupRepository.findBySlug(slug)
                .orElseThrow(() -> new GroupNotFoundException("group.with.slug.not.found.message", new Object[]{slug}));
    }

    public List<Group> getAllPublicGroups() {
        return groupRepository.findAllPublicGroups();
    }

    public List<Group> getGroupsByUser(Long userId) {
        return groupRepository.findGroupsByMemberId(userId);
    }

    public Group getGroupMembers(Long id) {
        Group group = groupRepository.findGroupWithMembers(id);
        if (group == null) {
            throw new GroupNotFoundException("group.not.found.message", new Object[]{id});
        }
        return group;
    }

    @Transactional
    public void transferOwnership(Long groupId, Long newOwnerId) {
        Group group = getById(groupId);
        User newOwner = userRegistratioService.get(newOwnerId);

        GroupMember oldOwnerMember = groupMemberService.getMember(groupId, group.getOwner().getId())
                .orElseThrow(() -> new GroupInvalidOperationException("group.old.owner.not.found.message"));

        if (oldOwnerMember.getMember().getId().equals(newOwnerId)) {
            throw new GroupInvalidOperationException("group.new.owner.same.as.current.message");
        }

        GroupMember newOwnerMember = groupMemberService.getMember(groupId, newOwnerId)
                .orElseThrow(() -> new GroupInvalidOperationException("group.new.owner.must.be.member.message"));

        if (newOwnerMember.getActive() && newOwnerMember.getRole().canBecomeOwner()) {
            group.setOwner(newOwner);
            groupRepository.save(group);

            newOwnerMember.promoteToOwner();
            oldOwnerMember.demoteToMember();
        } else {
            throw new GroupInvalidOperationException("group.new.owner.must.be.admin.message");
        }
    }

    @Transactional
    public void banGroup(Long groupId, Long bannedById, String reason, LocalDateTime expiresAt) {
        if (reason == null || reason.isBlank()) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.ban.reason.required.message",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        if (expiresAt != null && !expiresAt.isAfter(LocalDateTime.now())) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.ban.expires.invalid.message",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        Group group = getById(groupId);
        User bannedBy = userRegistratioService.get(bannedById);

        if (group.isBanned()) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.already.banned.message",
                    new Object[]{groupId},
                    HttpStatus.CONFLICT
            );
        }

        group.ban(bannedBy, reason.trim(), expiresAt);
    }

    @Transactional
    public void unbanGroup(Long groupId) {
        Group group = getById(groupId);

        if (!group.isBanned()) {
            throw new BusinessException(
                    "action.not.allowed.title",
                    "group.not.banned.message",
                    new Object[]{groupId},
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        group.unban();
    }

    public void validateRequiredFields(Group group) {
        boolean tagAlreadyExists = groupRepository.findByTag(group.getTag())
                .filter(g -> !g.equals(group))
                .isPresent();

        if (tagAlreadyExists) {
            throw new GroupConflictException("group.tag.already.exists.message", new Object[]{group.getTag()});
        }

        boolean slugAlreadyExists = groupRepository.findBySlug(group.getSlug())
                .filter(g -> !g.equals(group))
                .isPresent();

        if (slugAlreadyExists) {
            throw new GroupConflictException("group.slug.already.exists.message", new Object[]{group.getSlug()});
        }
    }

    @Override
    public Group get(Long groupId) {
        return getById(groupId);
    }

//    public Group getVoteTypes(Long groupId) {
//        groupRepository.findAllByVoteTypes(groupId);
//        return null;
//    }

}