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

    @Transactional
    public void setAsDefaultGroup(Long userId, Long groupId) {
        List<GroupMember> activeMembers = groupMemberRepository.findByMemberIdAndActiveTrue(userId);
        for (GroupMember gm : activeMembers) {
            gm.unselect();
        }

        GroupMember selectedMember = getMember(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));
        selectedMember.select();

        groupMemberRepository.saveAll(activeMembers);
        groupMemberRepository.save(selectedMember);
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
    public void removeMember(Long groupId, Long userId) {
        GroupMember member = getMember(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        if (member.getRole() == GroupMemberRole.OWNER) {
            throw new RuntimeException("Owner não pode ser removido do grupo");
        }

        member.disable();
    }

    @Transactional
    public void promoteToAdmin(Long groupId, Long userId) {
        GroupMember member = getMember(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        member.promoteToAdmin();
    }

    @Transactional
    public void demoteToMember(Long groupId, Long userId) {
        GroupMember member = getMember(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        member.demoteToMember();
    }

    @Transactional
    public void revokeAdmin(Long groupId, Long userId) {
        GroupMember member = getMember(groupId, userId)
                .orElseThrow(() -> new RuntimeException("Membro não encontrado"));

        member.demoteToMember();
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

}