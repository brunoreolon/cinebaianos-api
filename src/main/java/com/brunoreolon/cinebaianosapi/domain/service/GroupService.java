package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.OwnableService;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.repository.GroupRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));
    }

    public Group getGroupWithMovies(Long groupId) {
        return groupRepository.findGroupWithMovies(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));
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
        validateRequiredFields(group);
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

    public Group getById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException(String.format("Grupo com id %d não encontrado", groupId)));
    }

    public Group getBySlug(String slug) {
        return groupRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException(String.format("Grupo com slug %s não encontrado", slug)));

    }

    public List<Group> getAllPublicGroups() {
        return groupRepository.findAllPublicGroups();
    }

    public List<Group> getGroupsByUser(Long userId) {
        return groupRepository.findGroupsByMemberId(userId);
    }

    public Group getGroupMembers(Long id) {
        return groupRepository.findGroupWithMembers(id);
    }

    @Transactional
    public void transferOwnership(Long groupId, Long newOwnerId) {
        Group group = getById(groupId);
        User newOwner = userRegistratioService.get(newOwnerId);

        GroupMember oldOwnerMember = groupMemberService.getMember(groupId, group.getOwner().getId())
                .orElseThrow(() -> new RuntimeException("Antigo proprietário não encontrado"));

        if (oldOwnerMember.getMember().getId().equals(newOwnerId)) throw new RuntimeException("Novo proprietário é o mesmo que o atual");

        GroupMember newOwnerMember = groupMemberService.getMember(groupId, newOwnerId)
                .orElseThrow(() -> new RuntimeException("Novo proprietário deve ser um membro do grupo"));

        if (newOwnerMember.getActive() && newOwnerMember.getRole().canBecomeOwner()) {
            group.setOwner(newOwner);
            groupRepository.save(group);

            newOwnerMember.promoteToOwner();
            oldOwnerMember.demoteToMember();
        } else {
            throw new RuntimeException("Novo proprietário deve ser um administrador do grupo");
        }
    }

    private void validateRequiredFields(Group group) {
        boolean tagAlreadyExists = groupRepository.findByTag(group.getTag())
                .filter(g -> !g.equals(group))
                .isPresent();

        if (tagAlreadyExists) throw new RuntimeException(String.format("Tag %s já cadastrada", group.getTag()));

        boolean slugAlreadyExists = groupRepository.findBySlug(group.getSlug())
                .filter(g -> !g.equals(group))
                .isPresent();

        if (slugAlreadyExists) throw new RuntimeException(String.format("Slug %s já existe", group.getSlug()));
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