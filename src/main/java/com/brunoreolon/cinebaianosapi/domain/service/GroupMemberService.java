package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.ResourceKeyValues;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.GroupAuthorizationService;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.OwnableService;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.repository.GroupMemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class GroupMemberService implements GroupAuthorizationService, OwnableService<GroupMember, GroupMemberId> {

    private final GroupMemberRepository groupMemberRepository;
    private final GroupService groupService;
    private final UserRegistratioService userRegistratioService;

    public Optional<GroupMember> getMember(Long groupId, Long userId) {
        return groupMemberRepository.findByGroupIdAndMemberId(groupId, userId);
    }

    public GroupMember getMemberOrThrow(Long groupId, Long userId) {
        return getMember(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));
    }

    public List<GroupMember> getActiveByUser(Long userId) {
        return groupMemberRepository.findByMemberIdAndActiveTrueOrderByJoinedAtDesc(userId);
    }

    public List<Group> getActiveGroupsByUser(Long userId) {
        return getActiveByUser(userId).stream()
                .map(GroupMember::getGroup)
                .toList();
    }

    public Optional<GroupMember> getDefaultMembership(Long userId) {
        return groupMemberRepository.findByMemberIdAndActiveTrueAndSelectedTrue(userId);
    }

    public Optional<Group> getDefaultGroup(Long userId) {
        return getDefaultMembership(userId).map(GroupMember::getGroup);
    }

    @Transactional
    public void setAsDefaultGroup(Long userId, Long groupId) {
        List<GroupMember> activeMembers = getActiveByUser(userId);

        GroupMember selectedMember = null;

        for (GroupMember gm : activeMembers) {
            gm.unselect();

            if (gm.getGroupMemberId().getGroupId().equals(groupId)) {
                selectedMember = gm;
            }
        }

        if (selectedMember == null) {
            throw new RuntimeException("Membro não encontrado");
        }

        selectedMember.select();

        groupMemberRepository.saveAll(activeMembers);
    }

    @Transactional
    public GroupMember addMember(Group group, User user, GroupMemberRole role) {
        Optional<GroupMember> member = getMember(group.getId(), user.getId());

        List<GroupMember> activeMembers = groupMemberRepository.findByMemberIdAndActiveTrue(user.getId());
        for (GroupMember gm : activeMembers) {
            gm.unselect();
        }

        groupMemberRepository.saveAll(activeMembers);

        if (member.isPresent()) {
            GroupMember groupMember = member.get();

            if (groupMember.getActive())
                throw new RuntimeException("Usuário já é membro do grupo");

            groupMember.activate();
            groupMember.select();

            return groupMemberRepository.save(groupMember);
        }

        GroupMember newGroupMember = GroupMember.builder()
                .groupMemberId(new GroupMemberId(group.getId(), user.getId()))
                .member(user)
                .group(group)
                .role(role)
                .active(true)
                .selected(true)
                .build();

        return groupMemberRepository.save(newGroupMember);
    }

    @Transactional
    public GroupMember addMember(Long groupId, Long userId, GroupMemberRole role) {
        Group group = groupService.getById(groupId);
        User user = userRegistratioService.get(userId);

        return addMember(group, user, role);
    }

    @Transactional
    public GroupMember reactivateMember(Long groupId, Long userId) {
        GroupMember member = getMember(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        if (member.getActive()) {
            throw new RuntimeException("Usuário já é membro do grupo");
        }

        List<GroupMember> activeMembers = getActiveByUser(userId);
        for (GroupMember gm : activeMembers) {
            gm.unselect();
        }
        groupMemberRepository.saveAll(activeMembers);

        member.activate();
        member.select();

        return groupMemberRepository.save(member);
    }

    @Transactional
    public void removeMember(Long groupId, Long userId) {
        GroupMember member = getMemberOrThrow(groupId, userId);

        if (member.getRole() == GroupMemberRole.OWNER) {
            throw new RuntimeException("Owner não pode ser removido do grupo");
        }

        boolean selected = member.getSelected();
        member.disable();
        member.unselect();

        if (selected) {
            ensureDefaultGroupSelected(userId);
        }
    }

    @Transactional
    public void leaveGroup(Long groupId, Long userId) {
        removeMember(groupId, userId);
    }

    @Transactional
    public void promoteToAdmin(Long groupId, Long userId, Long userLoggedId) {
        if (userId.equals(userLoggedId)) throw new RuntimeException("Você não pode se promover para admin");

        GroupMember member = getMember(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        member.promoteToAdmin();
    }

    @Transactional
    public void demoteToMember(Long groupId, Long userId, Long userLoggedId) {
        if (userId.equals(userLoggedId)) throw new RuntimeException("Você não pode se rebaixar para membro");

        GroupMember member = getMember(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        member.demoteToMember();
    }

    @Transactional
    public void revokeAdmin(Long groupId, Long userId) {
        GroupMember member = getMemberOrThrow(groupId, userId);

        member.demoteToMember();
    }

    public GroupPermissions getPermissions(Long groupId, Long userId) {
        Optional<GroupMember> member = getMember(groupId, userId)
                .filter(GroupMember::getActive);

        if (member.isEmpty()) {
            return new GroupPermissions(false, null, false, false);
        }

        GroupMemberRole role = member.get().getRole();
        boolean canManage = role.atLeast(GroupMemberRole.ADMIN);
        boolean canTransferOwnership = role == GroupMemberRole.OWNER;

        return new GroupPermissions(true, role, canManage, canTransferOwnership);
    }

    public List<GroupMember> getActiveMembers(Long groupId) {
        return groupMemberRepository.findByGroupIdAndActiveTrue(groupId);
    }

    @Override
    public boolean isMember(Long groupId, Long userId) {
        return getMember(groupId, userId)
                .map(GroupMember::getActive)
                .orElse(false);
    }

    @Override
    public boolean hasRole(Long groupId, Long userId, GroupMemberRole requiredRole) {
        return getMember(groupId, userId)
                .map(m -> m.getActive() && m.getRole().atLeast(requiredRole))
                .orElse(false);
    }

    @Override
    public GroupMember get(GroupMemberId groupMemberId) {
        return getMember(groupMemberId.getGroupId(), groupMemberId.getMemberId())
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));
    }

    @Override
    public GroupMemberId buildId(ResourceKeyValues keyValues) {
        return keyValues.as(GroupMemberId.class);
    }

    @Override
    public String currentUserKeyName() {
        return "memberId";
    }

    private void ensureDefaultGroupSelected(Long userId) {
        List<GroupMember> activeMembers = getActiveByUser(userId);

        if (activeMembers.isEmpty()) {
            return;
        }

        boolean hasSelected = activeMembers.stream().anyMatch(GroupMember::getSelected);
        if (!hasSelected) {
            activeMembers.get(0).select();
            groupMemberRepository.save(activeMembers.get(0));
        }
    }

}