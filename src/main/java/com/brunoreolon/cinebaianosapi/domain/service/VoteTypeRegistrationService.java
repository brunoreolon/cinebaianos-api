package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.exception.EntityInUseException;
import com.brunoreolon.cinebaianosapi.domain.exception.VoteTypeAlreadyRegisteredException;
import com.brunoreolon.cinebaianosapi.domain.exception.VoteTypeNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import com.brunoreolon.cinebaianosapi.domain.repository.VoteTypeRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class VoteTypeRegistrationService {

    private final VoteTypeRepository voteTypeRepository;

    @Transactional
    public VoteType save(VoteType voteType) {
        if (voteType.getId() != null && !voteTypeRepository.existsById(voteType.getId()))
            throw new VoteTypeNotFoundException(String.format("VoteType with id '%d' not found", voteType.getId()));

        boolean nameAlreadyExists = voteTypeRepository.findByName(voteType.getName())
                .filter(type -> !type.equals(voteType))
                .isPresent();

        if (nameAlreadyExists)
            throw new VoteTypeAlreadyRegisteredException(String.format("there is already a VoteType registered with the name '%s'",
                    voteType.getName()));

        voteType.activate();

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
                .orElseThrow(() -> new VoteTypeNotFoundException(String.format("VoteType with id '%d' not found", id)));
    }

    public List<VoteType> getAll(Boolean active) {
        if (active == null) {
            return voteTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        }
        return voteTypeRepository.findAllByActiveOrderByIdAsc(active);
    }

    @Transactional
    public void delete(Long id) {
        VoteType voteType = get(id);

        try {
            voteTypeRepository.delete(voteType);
            voteTypeRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new EntityInUseException(String.format("Cannot delete vote type with id '%d' because it has associated votes.", id));
        }
    }

}
