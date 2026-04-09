package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.VoteTypeConverter;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeDetailResponse;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupMember;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.GroupKey;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMemberService;
import com.brunoreolon.cinebaianosapi.domain.service.VoteTypeRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/groups/{groupId}/vote-types")
@Tag(name = "Tipos de voto do grupo", description = "Operacoes de cadastro de tipos de voto no contexto de um grupo.")
public class GroupVoteTypeController {

    private final VoteTypeRegistrationService voteTypeRegistrationService;
    private final VoteTypeConverter voteTypeConverter;

    @CheckGroupMember(service = GroupMemberService.class)
    @GetMapping
    @Operation(summary = "Listar tipos de voto do grupo")
    public ResponseEntity<List<VoteTypeDetailResponse>> listGroupVoteTypes(
            @PathVariable @GroupKey Long groupId) {
        List<VoteType> votes = voteTypeRegistrationService.getAllByGroup(groupId, true);
        return ResponseEntity.ok(voteTypeConverter.toDetailResponseList(votes));
    }

    @CheckGroupMember(service = GroupMemberService.class)
    @GetMapping("/global")
    @Operation(summary = "Listar tipos de voto globais")
    public ResponseEntity<List<VoteTypeDetailResponse>> listGlobalVoteTypes(
            @PathVariable @GroupKey Long groupId) {
        List<VoteType> votes = voteTypeRegistrationService.getAllGlobal(true);
        return ResponseEntity.ok(voteTypeConverter.toDetailResponseList(votes));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PostMapping
    @Operation(summary = "Criar tipo de voto do grupo")
    public ResponseEntity<VoteTypeDetailResponse> createGroupVoteType(
            @PathVariable @GroupKey Long groupId,
            @Valid @RequestBody VoteTypeRequest voteTypeRequest) {
        VoteType voteType = voteTypeConverter.toEntityFromCreate(voteTypeRequest);
        VoteType created = voteTypeRegistrationService.createByGroup(groupId, voteType);
        return ResponseEntity.status(HttpStatus.CREATED).body(voteTypeConverter.toDetailResponse(created));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PutMapping("/{typeVoteId}")
    @Operation(summary = "Atualizar tipo de voto do grupo")
    public ResponseEntity<VoteTypeDetailResponse> updateGroupVoteType(
            @PathVariable @GroupKey Long groupId,
            @PathVariable Long typeVoteId,
            @Valid @RequestBody VoteTypeUpdateRequest voteTypeUpdateRequest) {
        VoteType input = voteTypeConverter.toEntityFromUpdate(voteTypeUpdateRequest);
        VoteType updated = voteTypeRegistrationService.updateByGroup(groupId, typeVoteId, input);
        return ResponseEntity.ok(voteTypeConverter.toDetailResponse(updated));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @DeleteMapping("/{typeVoteId}")
    @Operation(summary = "Excluir tipo de voto do grupo")
    public ResponseEntity<Void> deleteGroupVoteType(
            @PathVariable @GroupKey Long groupId,
            @PathVariable Long typeVoteId) {
        voteTypeRegistrationService.deleteByGroup(groupId, typeVoteId);
        return ResponseEntity.noContent().build();
    }

}