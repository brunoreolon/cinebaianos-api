package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.GroupConverter;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupDetailWithMembersResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupMemberResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupPermissionsResponse;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupMember;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckOwner;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.RequireRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.GroupKey;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.ResourceKey;
import com.brunoreolon.cinebaianosapi.domain.model.CustomUserDetails;
import com.brunoreolon.cinebaianosapi.domain.model.Group;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMember;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.domain.model.GroupPermissions;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMemberService;
import com.brunoreolon.cinebaianosapi.domain.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.brunoreolon.cinebaianosapi.core.security.authorization.enums.UserRole.SUPER_ADMIN;
import static com.brunoreolon.cinebaianosapi.core.security.authorization.enums.UserRole.USER;

@AllArgsConstructor
@RestController
@RequestMapping("/api/groups")
@Tag(name = "Membros de Grupos", description = "Operações relacionadas ao gerenciamento de membros em grupos.")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;
    private final GroupService groupService;
    private final GroupConverter groupConverter;

    @CheckGroupMember(service = GroupMemberService.class)
    @GetMapping("/{groupId}/members")
    @Operation(summary = "Obter membros do grupo",
            description = "Retorna um grupo com todos os seus membros ativos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membros do grupo encontrados", content = @Content(schema = @Schema(implementation = GroupDetailWithMembersResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
    })
    public ResponseEntity<GroupDetailWithMembersResponse> getGroupWithMembers(@PathVariable @GroupKey Long groupId) {
        Group group = groupService.getGroupMembers(groupId);
        GroupDetailWithMembersResponse response = groupConverter.toGroupWithMembersResponse(group);
        return ResponseEntity.ok(response);
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PostMapping("/{groupId}/members/{userId}")
    @Operation(summary = "Adicionar membro ao grupo",
            description = "Adiciona um usuário como membro do grupo com a role de MEMBER. Apenas o owner ou admin podem executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membro adicionado com sucesso", content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para adicionar membros"),
            @ApiResponse(responseCode = "404", description = "Grupo ou usuário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Usuário já é membro do grupo"),
    })
    public ResponseEntity<GroupMemberResponse> addMember(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "ID do usuário a ser adicionado", example = "1")
            @PathVariable Long userId) {
        GroupMember member = groupMemberService.addMember(groupId, userId, GroupMemberRole.MEMBER);
        return ResponseEntity.ok(groupConverter.toMemberResponse(member));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PostMapping("/{groupId}/members/{userId}/reactivate")
    @Operation(summary = "Reativar membro no grupo",
            description = "Reativa um membro inativo no grupo. Apenas owner ou admin podem executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membro reativado com sucesso", content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para reativar membros"),
            @ApiResponse(responseCode = "404", description = "Grupo ou membro não encontrado"),
            @ApiResponse(responseCode = "409", description = "Membro já está ativo no grupo"),
    })
    public ResponseEntity<GroupMemberResponse> reactivateMember(
            @PathVariable @GroupKey Long groupId,
            @PathVariable Long userId) {
        GroupMember member = groupMemberService.reactivateMember(groupId, userId);
        return ResponseEntity.ok(groupConverter.toMemberResponse(member));
    }

    @CheckGroupMember(service = GroupMemberService.class)
    @GetMapping("/{groupId}/members/{userId}")
    @Operation(summary = "Consultar membro específico do grupo",
            description = "Retorna os dados de papel e status de um membro específico do grupo.")
    public ResponseEntity<GroupMemberResponse> getMember(
            @PathVariable @GroupKey Long groupId,
            @PathVariable Long userId) {
        GroupMember member = groupMemberService.getMemberOrThrow(groupId, userId);
        return ResponseEntity.ok(groupConverter.toMemberResponse(member));
    }

    @RequireRole(roles = {USER, SUPER_ADMIN})
    @GetMapping("/{groupId}/permissions/me")
    @Operation(summary = "Consultar permissões do usuário logado no grupo",
            description = "Retorna permissões calculadas do usuário autenticado para o grupo informado.")
    public ResponseEntity<GroupPermissionsResponse> getMyPermissions(
            @PathVariable @GroupKey Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupPermissions permissions = groupMemberService.getPermissions(groupId, userDetails.getUser().getId());
        return ResponseEntity.ok(groupConverter.toPermissionsResponse(permissions));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @DeleteMapping("/{groupId}/members/{userId}")
    @Operation(summary = "Remover membro do grupo",
            description = "Remove um membro do grupo. O membro será marcado como inativo e seus dados permanecerão no sistema. Apenas o owner ou admin podem executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Membro removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para remover membros"),
            @ApiResponse(responseCode = "404", description = "Grupo ou membro não encontrado"),
            @ApiResponse(responseCode = "422", description = "Operação inválida: não é possível remover o owner ou o membro já está inativo"),
    })
    public ResponseEntity<Void> removeMember(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "ID do usuário a ser removido", example = "1")
            @PathVariable Long userId) {
        groupMemberService.removeMember(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @CheckGroupMember(service = GroupMemberService.class)
    @DeleteMapping("/{groupId}/members/me")
    @Operation(summary = "Sair do grupo",
            description = "Permite que o usuário autenticado saia do grupo informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Saída do grupo realizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é membro do grupo"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
            @ApiResponse(responseCode = "422", description = "Operação inválida: o owner não pode sair do grupo sem transferir a propriedade antes"),
    })
    public ResponseEntity<Void> leaveGroup(
            @PathVariable @GroupKey Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        groupMemberService.leaveGroup(groupId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @CheckOwner(service = GroupService.class)
    @PutMapping("/{groupId}/members/{userId}/promote-to-admin")
    @Operation(summary = "Promover membro para admin",
            description = "Promove um membro para admin do grupo. Apenas o owner pode executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membro promovido com sucesso", content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para promover membros"),
            @ApiResponse(responseCode = "404", description = "Grupo ou membro não encontrado"),
            @ApiResponse(responseCode = "422", description = "Operação inválida: não é possível promover a si mesmo ou o membro já é admin/owner"),
    })
    public ResponseEntity<GroupMemberResponse> promoteToAdmin(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @ResourceKey Long groupId,
            @Parameter(description = "ID do usuário a ser promovido", example = "1")
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        groupMemberService.promoteToAdmin(groupId, userId, userDetails.getUser().getId());
        GroupMember member = groupMemberService.getMemberOrThrow(groupId, userId);
        return ResponseEntity.ok(groupConverter.toMemberResponse(member));
    }

    @CheckOwner(service = GroupService.class)
    @PutMapping("/{groupId}/members/{userId}/demote-to-member")
    @Operation(summary = "Rebaixar admin para membro",
            description = "Rebaixa um admin para membro do grupo. Apenas o owner pode executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin rebaixado com sucesso", content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para rebaixar membros"),
            @ApiResponse(responseCode = "404", description = "Grupo ou membro não encontrado"),
            @ApiResponse(responseCode = "422", description = "Operação inválida: não é possível rebaixar a si mesmo ou o membro não é admin"),
    })
    public ResponseEntity<GroupMemberResponse> demoteToMember(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @ResourceKey Long groupId,
            @Parameter(description = "ID do admin a ser rebaixado", example = "1")
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        groupMemberService.demoteToMember(groupId, userId, userDetails.getUser().getId());
        GroupMember member = groupMemberService.getMemberOrThrow(groupId, userId);
        return ResponseEntity.ok(groupConverter.toMemberResponse(member));
    }
}