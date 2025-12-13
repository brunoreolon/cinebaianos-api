package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeStatusUpdateRequest;
import com.brunoreolon.cinebaianosapi.domain.model.Role;
import com.brunoreolon.cinebaianosapi.domain.service.VoteTypeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.*;

@RestController
@RequestMapping("/api/vote-types")
@AllArgsConstructor
public class VoteTypeController {

    private final VoteTypeService VoteTypeService;

    @PatchMapping("/{typeVoteId}")
    @RequireRole(roles = {Role.ADMIN})
    public ResponseEntity<Void> updateStatus(@PathVariable Long typeVoteId,
                                             @Valid @RequestBody VoteTypeStatusUpdateRequest active) {
        VoteTypeService.updateStatus(typeVoteId, active.getActive());
        return ResponseEntity.noContent().build();
    }
}
