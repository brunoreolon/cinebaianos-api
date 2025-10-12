package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class VoteTypeService {

    private final VoteTypeRegistrationService voteTypeRegistrationService;

    @Transactional
    public void updateStatus(Long id, boolean active) {
        if ((active == true)) {
            activate(id);
        } else {
            disable(id);
        }
    }

    private void activate(Long id) {
        VoteType voteType = voteTypeRegistrationService.get(id);
        voteType.activate();
    }

    private void disable(Long id) {
        VoteType voteType = voteTypeRegistrationService.get(id);
        voteType.disable();
    }

}
