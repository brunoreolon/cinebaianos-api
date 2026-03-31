package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.model.Group;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMovie;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.repository.GroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRegistratioService userRegistratioService;

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
    public Group save(Group group, String ownerId) {
        validatRequiredFields(group);

        User owner = userRegistratioService.get(ownerId);

        group.setOwner(owner);
        group.setActive(true);

        return groupRepository.save(group);
    }

    @Transactional
    public Group update(Group group) {
        validatRequiredFields(group);
        return groupRepository.save(group);
    }

    private void validatRequiredFields(Group group) {
        boolean tagAlreadyExists = groupRepository.findByTag(group.getTag())
                .filter(g -> !g.equals(group))
                .isPresent();

        if (tagAlreadyExists) throw new RuntimeException("Tag já existe");

        boolean slugAlreadyExists = groupRepository.findBySlug(group.getSlug())
                .filter(g -> !g.equals(group))
                .isPresent();

        if (slugAlreadyExists) throw new RuntimeException("Slug já existe");
    }

    public List<Group> getAll() {
        return groupRepository.findAll();
    }

    public Group getById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Grupo não encontrado"));
    }

//    public Group getVoteTypes(Long groupId) {
//        groupRepository.findAllByVoteTypes(groupId);
//        return null;
//    }

}