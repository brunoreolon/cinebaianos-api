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

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
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
        String normalizedSlug = normalizeSlug(slug);
        return groupRepository.findBySlugIgnoreCase(normalizedSlug)
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

        if (group.getOwner().getId().equals(newOwnerId)) {
            throw new GroupInvalidOperationException("group.new.owner.same.as.current.message");
        }

        User newOwner = userRegistratioService.get(newOwnerId);

        GroupMember oldOwnerMember = groupMemberService.getMember(groupId, group.getOwner().getId())
                .orElseThrow(() -> new GroupInvalidOperationException("group.old.owner.not.found.message"));

        GroupMember newOwnerMember = groupMemberService.getMember(groupId, newOwnerId)
                .orElseThrow(() -> new GroupInvalidOperationException("group.new.owner.must.be.member.message"));

        if (!Boolean.TRUE.equals(newOwnerMember.getActive())) {
            throw new GroupInvalidOperationException("group.new.owner.must.be.active.member.message");
        }

        if (groupMemberService.isBanned(groupId, newOwnerId)) {
            throw new GroupInvalidOperationException("group.member.banned.message", new Object[]{newOwnerId, groupId});
        }

        group.setOwner(newOwner);
        newOwnerMember.promoteToOwner();
        oldOwnerMember.demoteToMember();

        groupRepository.save(group);
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
        normalizeGroupFields(group);

        boolean tagAlreadyExists = hasConflictingTag(group.getTag(), group.getId());

        if (tagAlreadyExists) {
            throw new GroupConflictException("group.tag.already.exists.message", new Object[]{group.getTag()});
        }

        boolean slugAlreadyExists = hasConflictingSlug(group.getSlug(), group.getId());

        if (slugAlreadyExists) {
            throw new GroupConflictException("group.slug.already.exists.message", new Object[]{group.getSlug()});
        }
    }

    public boolean isTagAvailable(String tag) {
        return isTagAvailable(tag, null);
    }

    public boolean isTagAvailable(String tag, Long excludedGroupId) {
        String normalizedTag = normalizeTag(tag);
        return normalizedTag != null
                && !normalizedTag.isBlank()
                && !hasConflictingTag(normalizedTag, excludedGroupId);
    }

    public boolean isSlugAvailable(String slug) {
        return isSlugAvailable(slug, null);
    }

    public boolean isSlugAvailable(String slug, Long excludedGroupId) {
        String normalizedSlug = normalizeSlug(slug);
        return normalizedSlug != null
                && !normalizedSlug.isBlank()
                && !hasConflictingSlug(normalizedSlug, excludedGroupId);
    }

    public String normalizeTag(String tag) {
        if (tag == null) {
            return null;
        }

        return normalizeBasicText(tag)
                .replaceAll("[^A-Za-z0-9_]", "")
                .toUpperCase(Locale.ROOT);
    }

    public String normalizeSlug(String slug) {
        if (slug == null) {
            return null;
        }

        return normalizeBasicText(slug)
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9-]", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-+|-+$", "");
    }

    private void normalizeGroupFields(Group group) {
        if (group == null) {
            return;
        }

        group.setName(normalizeBasicText(group.getName()));
        group.setTag(normalizeTag(group.getTag()));
        group.setSlug(normalizeSlug(group.getSlug()));
    }

    private String normalizeBasicText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim();

        return normalized.isBlank() ? normalized : normalized.replaceAll("\\s+", " ");
    }

    private boolean hasConflictingTag(String tag, Long groupId) {
        if (tag == null || tag.isBlank()) {
            return false;
        }

        return groupId == null
                ? groupRepository.existsByTagIgnoreCase(tag)
                : groupRepository.existsByTagIgnoreCaseAndIdNot(tag, groupId);
    }

    private boolean hasConflictingSlug(String slug, Long groupId) {
        if (slug == null || slug.isBlank()) {
            return false;
        }

        return groupId == null
                ? groupRepository.existsBySlugIgnoreCase(slug)
                : groupRepository.existsBySlugIgnoreCaseAndIdNot(slug, groupId);
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