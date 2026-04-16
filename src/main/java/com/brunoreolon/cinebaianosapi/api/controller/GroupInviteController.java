package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.GroupAccessConverter;
import com.brunoreolon.cinebaianosapi.api.converter.GroupConverter;
import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.request.AcceptGroupInviteRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupInviteCreateRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupInviteCandidateSliceResponse;
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
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.service.GroupInviteService;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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
    @Operation(summary = "Criar convite do grupo", description = "Cria um convite específico ou genérico para entrada no grupo. Apenas administradores do grupo podem executar esta operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Convite criado com sucesso", content = @Content(schema = @Schema(implementation = GroupInviteResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para criar convites", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo ou usuário convidado não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe convite pendente conflitante", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Regra de convite inválida para o grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<GroupInviteResponse> createInvite(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "Dados do convite a ser criado")
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
    @Operation(summary = "Listar convites pendentes do grupo", description = "Retorna os convites pendentes do grupo informado. Apenas administradores do grupo podem executar esta operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convites pendentes retornados com sucesso", content = @Content(schema = @Schema(implementation = GroupInviteResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para listar convites", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<GroupInviteResponse>> getPendingInvites(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId) {
        List<GroupInviteResponse> response = groupInviteService.getPendingInvitesByGroup(groupId)
                .stream()
                .map(groupAccessConverter::toInviteResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @GetMapping("/{groupId}/invite-candidates")
    @Operation(summary = "Buscar candidatos a convite do grupo", description = "Retorna usuários elegíveis para convite direto no grupo, filtrando por nome ou e-mail com paginação leve.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Candidatos retornados com sucesso", content = @Content(schema = @Schema(implementation = GroupInviteCandidateSliceResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para buscar candidatos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<GroupInviteCandidateSliceResponse> searchInviteCandidates(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "Trecho do nome ou e-mail do usuário", example = "bruno")
            @RequestParam(value = "q", required = false, defaultValue = "") String query,
            @Parameter(description = "Página da busca", example = "0")
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @Parameter(description = "Quantidade máxima de resultados", example = "10")
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = Math.min(Math.max(size, 1), 20);
        Pageable pageable = PageRequest.of(sanitizedPage, sanitizedSize);

        Slice<User> candidates = groupInviteService.searchInviteCandidates(groupId, query, pageable);
        return ResponseEntity.ok(groupAccessConverter.toInviteCandidateSliceResponse(candidates));
    }

    @RequireMinimumRole(role = USER)
    @GetMapping("/invites/received")
    @Operation(summary = "Listar convites recebidos", description = "Retorna os convites pendentes recebidos pelo usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convites recebidos retornados com sucesso", content = @Content(schema = @Schema(implementation = GroupInviteResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
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
    @Operation(summary = "Revogar convite do grupo", description = "Revoga um convite pendente do grupo informado. Apenas administradores do grupo podem executar esta operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Convite revogado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para revogar convites", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo ou convite não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> revokeInvite(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "ID do convite", example = "25")
            @PathVariable Long inviteId) {
        groupInviteService.revokeInvite(groupId, inviteId);
        return ResponseEntity.noContent().build();
    }

    @RequireMinimumRole(role = USER)
    @PostMapping("/invites/accept")
    @Operation(summary = "Aceitar convite por token", description = "Aceita um convite recebido ou genérico utilizando o token informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Convite aceito com sucesso", content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Convite não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Convite já foi utilizado ou o usuário já é membro do grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Convite expirado, cancelado ou incompatível com o usuário", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<GroupMemberResponse> acceptInvite(
            @Parameter(description = "Token do convite a ser aceito")
            @Valid @RequestBody AcceptGroupInviteRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupMember member = groupInviteService.acceptInvite(request.getToken(), userDetails.getUser().getId());
        return ResponseEntity.ok(groupConverter.toMemberResponse(member));
    }

    @RequireMinimumRole(role = USER)
    @DeleteMapping("/invites/{inviteId}/decline")
    @Operation(summary = "Recusar convite recebido", description = "Recusa um convite pendente recebido pelo usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Convite recusado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Convite não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Convite não pertence ao usuário autenticado ou não está pendente", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> declineInvite(
            @Parameter(description = "ID do convite recebido", example = "25")
            @PathVariable Long inviteId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        groupInviteService.declineInvite(inviteId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

}