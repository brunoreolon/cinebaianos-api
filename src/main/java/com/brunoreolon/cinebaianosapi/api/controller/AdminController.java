package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserResetPasswordRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserStatusActiveAccountUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserStatusAdminUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.user.request.UserBanRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupResponse;
import com.brunoreolon.cinebaianosapi.api.converter.GroupConverter;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeStatusUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupBanRequest;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.ResourceKey;
import com.brunoreolon.cinebaianosapi.domain.model.CustomUserDetails;
import com.brunoreolon.cinebaianosapi.domain.service.GroupService;
import com.brunoreolon.cinebaianosapi.domain.service.UserService;
import com.brunoreolon.cinebaianosapi.domain.service.VoteTypeService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.*;
import static com.brunoreolon.cinebaianosapi.core.security.authorization.enums.UserRole.*;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@Tag(name = "Admin", description = "Operações administrativas, incluindo gerenciamento de usuários e tipos de voto.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class AdminController {

    private final UserService userService;
    private final GroupService groupService;
    private final VoteTypeService voteTypeService;
    private final GroupConverter groupConverter;

    @RequireMinimumRole(role = SUPER_ADMIN)
    @GetMapping("/groups")
    @Operation(summary = "Listar grupos para administração",
            description = "Retorna todos os grupos cadastrados no sistema para visão administrativa global.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Grupos retornados com sucesso", content = @Content(schema = @Schema(implementation = GroupResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para visualizar grupos administrativamente", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<GroupResponse>> listGroups() {
        return ResponseEntity.ok(groupConverter.toResponseList(groupService.getAllForAdmin()));
    }

    @RequireMinimumRole(role = SUPER_ADMIN)
    @PostMapping("/users/{userId}/reset-password")
    @Operation(
            summary = "Resetar senha de usuário",
            description = "Permite que um administrador redefina a senha de um usuário específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha resetada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para esta ação", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "ID do usuário que terá a senha resetada", example = "1")
            @PathVariable @ResourceKey Long userId,

            @Parameter(description = "Nova senha do usuário")
            @Valid @RequestBody UserResetPasswordRequest passwordRequest) {
        userService.resetPasswordByAdmin(userId, passwordRequest.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @RequireMinimumRole(role = SUPER_ADMIN)
    @PostMapping("/users/{userId}/activation")
    @Operation(
            summary = "Ativar/Desativar usuário",
            description = "Permite que um administrador ative ou desative a conta de um usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status do usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para esta ação", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> updateStatusActiveAccount(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId,

            @Parameter(description = "Objeto contendo o novo status de ativação")
            @Valid @RequestBody UserStatusActiveAccountUpdateRequest active) {
        userService.changeActivationStatus(userId, active.getActive());
        return ResponseEntity.noContent().build();
    }

    @RequireMinimumRole(role = SUPER_ADMIN)
    @PostMapping("/users/{userId}/admin")
    @Operation(
            summary = "Conceder/Remover privilégios de administrador",
            description = "Permite que um administrador atualize o status de administrador de outro usuário."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status de administrador atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para esta ação", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> updateStatusAdmin(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId,

            @Parameter(description = "Objeto contendo o novo status de administrador")
            @Valid @RequestBody UserStatusAdminUpdateRequest admin,

            @Parameter(description = "Usuário autenticado (injetado pelo Spring Security)")
            @AuthenticationPrincipal UserDetails userDetails) {
        userService.updateStatusAdmin(userDetails.getUsername(), userId, admin.getAdmin());
        return ResponseEntity.noContent().build();
    }

    @RequireMinimumRole(role = SUPER_ADMIN)
    @PostMapping("/vote-types/{typeVoteId}/activation")
    @Operation(
            summary = "Ativar/Desativar tipo de voto",
            description = "Permite que um administrador altere o status de um tipo de voto específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status do tipo de voto atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para esta ação", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tipo de voto não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> updateStatusVoteType(
            @Parameter(description = "ID do tipo de voto", example = "1")
            @PathVariable Long typeVoteId,

            @Parameter(description = "Objeto contendo o novo status de ativação")
            @Valid @RequestBody VoteTypeStatusUpdateRequest active) {
        voteTypeService.updateStatus(typeVoteId, active.getActive());
        return ResponseEntity.noContent().build();
    }

    @RequireMinimumRole(role = SUPER_ADMIN)
    @PostMapping("/users/{userId}/ban")
    @Operation(summary = "Banir usuario no sistema",
            description = "Permite banimento temporario ou definitivo de usuario no contexto global do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário banido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para banir usuários", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Usuário já possui banimento ativo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Operação inválida para o usuário informado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> banUser(
            @Parameter(description = "ID do usuário a ser banido", example = "1")
            @PathVariable @ResourceKey Long userId,
            @Parameter(description = "Dados do banimento do usuário")
            @Valid @RequestBody UserBanRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.banUser(userId, userDetails.getUser().getId(), request.getReason(), request.getExpiresAt());
        return ResponseEntity.noContent().build();
    }

    @RequireMinimumRole(role = SUPER_ADMIN)
    @DeleteMapping("/users/{userId}/ban")
    @Operation(summary = "Remover banimento de usuario no sistema",
            description = "Remove banimento global ativo de um usuario.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Banimento do usuário removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para remover banimento", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário ou banimento não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> unbanUser(
            @Parameter(description = "ID do usuário com banimento ativo", example = "1")
            @PathVariable @ResourceKey Long userId) {
        userService.unbanUser(userId);
        return ResponseEntity.noContent().build();
    }

    @RequireMinimumRole(role = SUPER_ADMIN)
    @PostMapping("/groups/{groupId}/ban")
    @Operation(summary = "Banir grupo no sistema",
            description = "Permite banimento temporario ou definitivo de um grupo no contexto global do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Grupo banido com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para banir grupos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Grupo já possui banimento ativo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Operação inválida para o grupo informado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> banGroup(
            @Parameter(description = "ID do grupo a ser banido", example = "1")
            @PathVariable @ResourceKey Long groupId,
            @Parameter(description = "Dados do banimento do grupo")
            @Valid @RequestBody GroupBanRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        groupService.banGroup(groupId, userDetails.getUser().getId(), request.getReason(), request.getExpiresAt());
        return ResponseEntity.noContent().build();
    }

    @RequireMinimumRole(role = SUPER_ADMIN)
    @DeleteMapping("/groups/{groupId}/ban")
    @Operation(summary = "Remover banimento de grupo no sistema",
            description = "Remove banimento global ativo de um grupo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Banimento do grupo removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para remover banimento de grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo ou banimento não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> unbanGroup(
            @Parameter(description = "ID do grupo com banimento ativo", example = "1")
            @PathVariable @ResourceKey Long groupId) {
        groupService.unbanGroup(groupId);
        return ResponseEntity.noContent().build();
    }

}