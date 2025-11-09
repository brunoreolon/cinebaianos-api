package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.VoteTypeConverter;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeDetailResponse;
import com.brunoreolon.cinebaianosapi.core.security.CheckSecurity;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import com.brunoreolon.cinebaianosapi.domain.service.VoteTypeRegistrationService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vote-types")
@AllArgsConstructor
public class VoteTypeRegistrationController {

    private final VoteTypeRegistrationService voteTypeRegistrationService;
    private final VoteTypeConverter voteTypeConverter;

    @PostMapping
    @CheckSecurity.IsAdmin
    public ResponseEntity<VoteTypeDetailResponse> create(@Valid @RequestBody VoteTypeRequest voteTypeRequest) {
        VoteType voteType = voteTypeConverter.toEntityFromCreate(voteTypeRequest);
        VoteType newVoteType = voteTypeRegistrationService.save(voteType);

        return ResponseEntity.status(HttpStatus.CREATED).body(voteTypeConverter.toDetailResponse(newVoteType));
    }

    @GetMapping
    @CheckSecurity.CanAccess
    public ResponseEntity<List<VoteTypeDetailResponse>> getAll(
            @RequestParam(name = "active", defaultValue = "true") Boolean active) {
        List<VoteType> voteTypes = voteTypeRegistrationService.getAll(active);
        return ResponseEntity.ok().body(voteTypeConverter.toDetailResponseList(voteTypes));
    }

    @GetMapping("/{typeVoteId}")
    @CheckSecurity.CanAccess
    public ResponseEntity<VoteTypeDetailResponse> get(@PathVariable Long typeVoteId) {
        VoteType voteType = voteTypeRegistrationService.get(typeVoteId);
        return ResponseEntity.ok().body(voteTypeConverter.toDetailResponse(voteType));
    }

    @DeleteMapping("/{typeVoteId}")
    @CheckSecurity.IsAdmin
    public ResponseEntity<Void> delete(@PathVariable Long typeVoteId) {
        voteTypeRegistrationService.delete(typeVoteId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{typeVoteId}")
    @CheckSecurity.IsAdmin
    public ResponseEntity<VoteTypeDetailResponse> edit(@PathVariable Long typeVoteId,
                                                       @Valid @RequestBody VoteTypeUpdateRequest voteTypeUpdateRequest) {
        VoteType voteType = voteTypeConverter.toEntityFromUpdate(voteTypeUpdateRequest);
        voteType.setId(typeVoteId);

        VoteType voteTypeUpdated = voteTypeRegistrationService.save(voteType);

        return ResponseEntity.ok().body(voteTypeConverter.toDetailResponse(voteTypeUpdated));
    }

}
