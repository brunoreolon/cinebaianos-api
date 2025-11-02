package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeStatusUpdateRequest;
import com.brunoreolon.cinebaianosapi.core.security.CheckSecurity;
import com.brunoreolon.cinebaianosapi.domain.service.VoteTypeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vote-types")
@AllArgsConstructor
public class VoteTypeController {

    private final VoteTypeService VoteTypeService;

    @PatchMapping("/{typeVoteId}")
    @CheckSecurity.IsAdmin
    public ResponseEntity<Void> updateStatus(@PathVariable Long typeVoteId,
                                             @Valid @RequestBody VoteTypeStatusUpdateRequest active) {
        VoteTypeService.updateStatus(typeVoteId, active.getActive());
        return ResponseEntity.noContent().build();
    }
}
