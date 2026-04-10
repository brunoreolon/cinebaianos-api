package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.GroupAccessConverter;
import com.brunoreolon.cinebaianosapi.api.converter.GroupConverter;
import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    @Operation(summary = "Entrar em grupo OPEN", description = "Adiciona o usuário autenticado diretamente em um grupo com política de entrada OPEN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Entrada no grupo realizada com sucesso", content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Usuário já é membro do grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Grupo não permite entrada direta", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<GroupMemberResponse> joinOpenGroup(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupMember member = groupJoinRequestService.joinOpenGroup(groupId, userDetails.getUser().getId());
        return ResponseEntity.ok(groupConverter.toMemberResponse(member));
    }

    @RequireMinimumRole(role = USER)
    @PostMapping("/{groupId}/join-requests")
    @Operation(summary = "Criar solicitação para grupo REQUEST", description = "Cria uma solicitação de entrada para um grupo com política REQUEST.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Solicitação criada com sucesso", content = @Content(schema = @Schema(implementation = GroupJoinRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe solicitação pendente ou o usuário já é membro do grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Grupo não aceita solicitações de entrada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<GroupJoinRequestResponse> createJoinRequest(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupJoinRequest request = groupJoinRequestService.createJoinRequest(groupId, userDetails.getUser().getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(groupAccessConverter.toJoinRequestResponse(request));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @GetMapping("/{groupId}/join-requests")
    @Operation(summary = "Listar solicitações pendentes", description = "Retorna as solicitações de entrada pendentes do grupo informado. Apenas administradores do grupo podem executar esta operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitações pendentes retornadas com sucesso", content = @Content(schema = @Schema(implementation = GroupJoinRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para listar solicitações", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<GroupJoinRequestResponse>> getPendingRequests(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId) {
        List<GroupJoinRequestResponse> response = groupJoinRequestService.getPendingRequestsByGroup(groupId)
                .stream()
                .map(groupAccessConverter::toJoinRequestResponse)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    @RequireMinimumRole(role = USER)
    @GetMapping("/{groupId}/join-requests/me")
    @Operation(summary = "Consultar minha solicitação pendente", description = "Retorna a solicitação pendente do usuário autenticado para o grupo informado, quando existir.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitação pendente encontrada", content = @Content(schema = @Schema(implementation = GroupJoinRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Solicitação pendente não encontrada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<GroupJoinRequestResponse> getMyPendingRequest(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupJoinRequest request = groupJoinRequestService.getMyPendingRequest(groupId, userDetails.getUser().getId());
        return ResponseEntity.ok(groupAccessConverter.toJoinRequestResponse(request));
    }

    @RequireMinimumRole(role = USER)
    @DeleteMapping("/{groupId}/join-requests/me")
    @Operation(summary = "Cancelar minha solicitação pendente", description = "Cancela a solicitação de entrada pendente do usuário autenticado para o grupo informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Solicitação cancelada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Solicitação pendente não encontrada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> cancelMyPendingRequest(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        groupJoinRequestService.cancelMyPendingRequest(groupId, userDetails.getUser().getId());
        return ResponseEntity.noContent().build();
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PutMapping("/{groupId}/join-requests/{requestId}/approve")
    @Operation(summary = "Aprovar solicitação de entrada", description = "Aprova uma solicitação de entrada pendente no grupo informado. Apenas administradores do grupo podem executar esta operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitação aprovada com sucesso", content = @Content(schema = @Schema(implementation = GroupJoinRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para aprovar solicitações", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo ou solicitação não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Solicitação já foi processada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<GroupJoinRequestResponse> approve(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "ID da solicitação", example = "25")
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupJoinRequest request = groupJoinRequestService.approve(groupId, requestId, userDetails.getUser().getId());
        return ResponseEntity.ok(groupAccessConverter.toJoinRequestResponse(request));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PutMapping("/{groupId}/join-requests/{requestId}/reject")
    @Operation(summary = "Rejeitar solicitação de entrada", description = "Rejeita uma solicitação de entrada pendente no grupo informado. Apenas administradores do grupo podem executar esta operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Solicitação rejeitada com sucesso", content = @Content(schema = @Schema(implementation = GroupJoinRequestResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para rejeitar solicitações", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo ou solicitação não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Solicitação já foi processada", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<GroupJoinRequestResponse> reject(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "ID da solicitação", example = "25")
            @PathVariable Long requestId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        GroupJoinRequest request = groupJoinRequestService.reject(groupId, requestId, userDetails.getUser().getId());
        return ResponseEntity.ok(groupAccessConverter.toJoinRequestResponse(request));
    }

}