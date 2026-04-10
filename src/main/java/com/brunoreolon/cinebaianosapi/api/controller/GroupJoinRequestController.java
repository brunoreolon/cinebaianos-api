package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.GroupAccessConverter;
import com.brunoreolon.cinebaianosapi.api.converter.GroupConverter;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupJoinRequestResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupMemberResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.RequireMinimumRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.GroupKey;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.domain.model.CustomUserDetails;
import com.brunoreolon.cinebaianosapi.domain.model.GroupJoinRequest;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMember;
import com.brunoreolon.cinebaianosapi.domain.service.GroupJoinRequestService;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.brunoreolon.cinebaianosapi.core.security.authorization.enums.UserRole.USER;

@AllArgsConstructor
@RestController
@RequestMapping("/api/groups")
@Tag(name = "Solicitacoes de Entrada", description = "Operacoes para entrar em grupos com politica OPEN e REQUEST.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class GroupJoinRequestController {

    private final GroupJoinRequestService groupJoinRequestService;
    private final GroupConverter groupConverter;
    private final GroupAccessConverter groupAccessConverter;

    @RequireMinimumRole(role = USER)
    @PostMapping("/{groupId}/join")
    @Operation(summary = "Entrar em grupo OPEN")
    public ResponseEntity<GroupMemberResponse> joinOpenGroup(
            @PathVariable @GroupKey Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupMember member = groupJoinRequestService.joinOpenGroup(groupId, userDetails.getUser().getId());
        return ResponseEntity.ok(groupConverter.toMemberResponse(member));
    }

    @RequireMinimumRole(role = USER)
    @PostMapping("/{groupId}/join-requests")
    @Operation(summary = "Criar solicitacao para grupo REQUEST")
    public ResponseEntity<GroupJoinRequestResponse> createJoinRequest(
            @PathVariable @GroupKey Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupJoinRequest request = groupJoinRequestService.createJoinRequest(groupId, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(groupAccessConverter.toJoinRequestResponse(request));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @GetMapping("/{groupId}/join-requests")
    @Operation(summary = "Listar solicitacoes pendentes")
    public ResponseEntity<List<GroupJoinRequestResponse>> getPendingRequests(
            @PathVariable @GroupKey Long groupId) {
        List<GroupJoinRequestResponse> response = groupJoinRequestService.getPendingRequestsByGroup(groupId)
                .stream()
                .map(groupAccessConverter::toJoinRequestResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @RequireMinimumRole(role = USER)
    @GetMapping("/{groupId}/join-requests/me")
    @Operation(summary = "Consultar minha solicitacao pendente")
    public ResponseEntity<GroupJoinRequestResponse> getMyPendingRequest(
            @PathVariable @GroupKey Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupJoinRequest request = groupJoinRequestService.getMyPendingRequest(groupId, userDetails.getUser().getId());
        return ResponseEntity.ok(groupAccessConverter.toJoinRequestResponse(request));
    }

    @RequireMinimumRole(role = USER)
    @DeleteMapping("/{groupId}/join-requests/me")
    @Operation(summary = "Cancelar minha solicitacao pendente")
    public ResponseEntity<Void> cancelMyPendingRequest(
            @PathVariable @GroupKey Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        groupJoinRequestService.cancelMyPendingRequest(groupId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PutMapping("/{groupId}/join-requests/{requestId}/approve")
    @Operation(summary = "Aprovar solicitacao de entrada")
    public ResponseEntity<GroupJoinRequestResponse> approve(
            @PathVariable @GroupKey Long groupId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupJoinRequest request = groupJoinRequestService.approve(groupId, requestId, userDetails.getUser().getId());
        return ResponseEntity.ok(groupAccessConverter.toJoinRequestResponse(request));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PutMapping("/{groupId}/join-requests/{requestId}/reject")
    @Operation(summary = "Rejeitar solicitacao de entrada")
    public ResponseEntity<GroupJoinRequestResponse> reject(
            @PathVariable @GroupKey Long groupId,
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupJoinRequest request = groupJoinRequestService.reject(groupId, requestId, userDetails.getUser().getId());
        return ResponseEntity.ok(groupAccessConverter.toJoinRequestResponse(request));
    }

}