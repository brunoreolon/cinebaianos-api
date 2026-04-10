package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.GroupAccessConverter;
import com.brunoreolon.cinebaianosapi.api.converter.GroupConverter;
import com.brunoreolon.cinebaianosapi.api.model.group.request.AcceptGroupInviteRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupInviteCreateRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupInviteResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupMemberResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.RequireMinimumRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.GroupKey;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.domain.model.CustomUserDetails;
import com.brunoreolon.cinebaianosapi.domain.model.GroupInvite;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMember;
import com.brunoreolon.cinebaianosapi.domain.service.GroupInviteService;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@Tag(name = "Convites de Grupo", description = "Operacoes de convites para entrada em grupos.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class GroupInviteController {

    private final GroupInviteService groupInviteService;
    private final GroupAccessConverter groupAccessConverter;
    private final GroupConverter groupConverter;

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PostMapping("/{groupId}/invites")
    @Operation(summary = "Criar convite do grupo")
    public ResponseEntity<GroupInviteResponse> createInvite(
            @PathVariable @GroupKey Long groupId,
            @Valid @RequestBody GroupInviteCreateRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupInvite invite = groupInviteService.createInvite(
                groupId,
                userDetails.getUser().getId(),
                request.getInvitedUserId(),
                request.getMaxUses(),
                request.getExpiresAt()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(groupAccessConverter.toInviteResponse(invite));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @GetMapping("/{groupId}/invites")
    @Operation(summary = "Listar convites pendentes do grupo")
    public ResponseEntity<List<GroupInviteResponse>> getPendingInvites(@PathVariable @GroupKey Long groupId) {
        List<GroupInviteResponse> response = groupInviteService.getPendingInvitesByGroup(groupId)
                .stream()
                .map(groupAccessConverter::toInviteResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @RequireMinimumRole(role = USER)
    @GetMapping("/invites/received")
    @Operation(summary = "Listar convites recebidos")
    public ResponseEntity<List<GroupInviteResponse>> getReceivedInvites(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<GroupInviteResponse> response = groupInviteService.getPendingReceivedInvites(userDetails.getUser().getId())
                .stream()
                .map(groupAccessConverter::toInviteResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @DeleteMapping("/{groupId}/invites/{inviteId}")
    @Operation(summary = "Revogar convite do grupo")
    public ResponseEntity<Void> revokeInvite(
            @PathVariable @GroupKey Long groupId,
            @PathVariable Long inviteId) {
        groupInviteService.revokeInvite(groupId, inviteId);
        return ResponseEntity.noContent().build();
    }

    @RequireMinimumRole(role = USER)
    @PostMapping("/invites/accept")
    @Operation(summary = "Aceitar convite por token")
    public ResponseEntity<GroupMemberResponse> acceptInvite(
            @Valid @RequestBody AcceptGroupInviteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupMember member = groupInviteService.acceptInvite(request.getToken(), userDetails.getUser().getId());
        return ResponseEntity.ok(groupConverter.toMemberResponse(member));
    }

    @RequireMinimumRole(role = USER)
    @DeleteMapping("/invites/{inviteId}/decline")
    @Operation(summary = "Recusar convite recebido")
    public ResponseEntity<Void> declineInvite(
            @PathVariable Long inviteId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        groupInviteService.declineInvite(inviteId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

}