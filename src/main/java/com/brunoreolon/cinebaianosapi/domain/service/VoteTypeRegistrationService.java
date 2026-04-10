package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.exception.EntityInUseException;
import com.brunoreolon.cinebaianosapi.domain.exception.BusinessException;
import com.brunoreolon.cinebaianosapi.domain.exception.VoteTypeAlreadyRegisteredException;
import com.brunoreolon.cinebaianosapi.domain.exception.VoteTypeNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.Group;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import com.brunoreolon.cinebaianosapi.domain.repository.VoteTypeRepository;
import com.brunoreolon.cinebaianosapi.util.ApiErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class VoteTypeRegistrationService {

    private final VoteTypeRepository voteTypeRepository;
    private final GroupService groupService;

    @Transactional
    public VoteType save(VoteType voteType) {
        if (voteType.getId() != null && !voteTypeRepository.existsById(voteType.getId()))
            throw new VoteTypeNotFoundException("vote.type.not.found.message", new Object[]{voteType.getId()});

        boolean nameAlreadyExists = voteTypeRepository.findByName(voteType.getName())
                .filter(type -> !type.equals(voteType))
                .isPresent();

        if (nameAlreadyExists)
            throw new VoteTypeAlreadyRegisteredException("vote-type.already.registered.message", new Object[]{voteType.getName()});

        if (voteType.getColor() == null || voteType.getColor().isBlank())
            voteType.setColor("#9810fa");

        if (voteType.getEmoji() == null || voteType.getEmoji().isBlank())
            voteType.setEmoji("⭐");

        return voteTypeRepository.save(voteType);
    }

    public Optional<VoteType> getOptional(Long id) {
        return voteTypeRepository.findById(id);
    }

    public VoteType get(Long id) {
        return getOptional(id)
                .orElseThrow(() -> new VoteTypeNotFoundException("vote.type.not.found.message", new Object[]{id}));
    }

    public List<VoteType> getAll(Boolean active) {
        if (active == null) {
            return voteTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        }
        return voteTypeRepository.findAllByActiveOrderByIdAsc(active);
    }

    public List<VoteType> getAllGlobal(Boolean active) {
        return voteTypeRepository.findAllByGroupIsNullAndActiveOrderByIdAsc(active);
    }

    public List<VoteType> getAllByGroup(Long groupId, Boolean active) {
        return voteTypeRepository.findAllByGroupIdAndActiveOrderByIdAsc(groupId, active);
    }

    @Transactional
    public VoteType createByGroup(Long groupId, VoteType voteType) {
        Group group = groupService.get(groupId);
        voteType.setGroup(group);
        return save(voteType);
    }

    @Transactional
    public VoteType updateByGroup(Long groupId, Long voteTypeId, VoteType voteTypeInput) {
        VoteType existing = get(voteTypeId);
        ensureVoteTypeBelongsToGroup(existing, groupId);

        existing.setName(voteTypeInput.getName());
        existing.setDescription(voteTypeInput.getDescription());
        existing.setColor(voteTypeInput.getColor());
        existing.setEmoji(voteTypeInput.getEmoji());

        return save(existing);
    }

    @Transactional
    public void deleteByGroup(Long groupId, Long voteTypeId) {
        VoteType existing = get(voteTypeId);
        ensureVoteTypeBelongsToGroup(existing, groupId);
        delete(voteTypeId);
    }

    @Transactional
    public void delete(Long id) {
        VoteType voteType = get(id);

        try {
            voteTypeRepository.delete(voteType);
            voteTypeRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new EntityInUseException("vote-type.in.use.title", new Object[]{id}, ApiErrorCode.VOTE_TYPE_IN_USE);
        }
    }

    private void ensureVoteTypeBelongsToGroup(VoteType voteType, Long groupId) {
        if (voteType.getGroup() == null || !voteType.getGroup().getId().equals(groupId)) {
            throw new BusinessException(
                    "vote.type.not.in.group.title",
                    "vote.type.not.in.group.message",
                    new Object[]{voteType.getId(), groupId},
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    ApiErrorCode.VOTE_TYPE_NOT_IN_GROUP.asMap()
            );
        }
    }

}