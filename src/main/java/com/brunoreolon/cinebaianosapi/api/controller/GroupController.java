package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.GroupConverter;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupMember;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckOwner;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.RequireRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.GroupKey;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.ResourceKey;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMemberService;
import com.brunoreolon.cinebaianosapi.domain.service.GroupService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.brunoreolon.cinebaianosapi.core.security.authorization.enums.UserRole.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/groups")
@Tag(name = "Grupos", description = "Operações relacionadas ao gerenciamento de grupos de usuários e membros.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class GroupController {

    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final GroupConverter groupConverter;

    @RequireRole(roles = {USER, SUPER_ADMIN})
    @PostMapping
    @Operation(summary = "Criar novo grupo",
            description = "Cria um novo grupo. O usuário autenticado será definido como o owner do grupo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Grupo criado com sucesso", content = @Content(schema = @Schema(implementation = GroupResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "409", description = "Tag ou slug já existente em outro grupo"),
    })
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        Group group = groupConverter.toEntiy(request);

        Long ownerId = userDetails.getUser().getId();

        Group newGroup = groupService.save(group, ownerId);

        return ResponseEntity.status(HttpStatus.CREATED).body(groupConverter.toResponse(newGroup));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PutMapping("/{groupId}")
    @Operation(summary = "Atualizar grupo",
            description = "Atualiza as configurações de um grupo existente. Apenas o owner ou admin do grupo podem executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grupo atualizado com sucesso", content = @Content(schema = @Schema(implementation = GroupResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para atualizar este grupo"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
            @ApiResponse(responseCode = "409", description = "Tag ou slug já existente em outro grupo"),
    })
    public ResponseEntity<GroupResponse> update(@PathVariable @GroupKey Long groupId,
                                                @Valid @RequestBody GroupUpdateRequest request) {
        Group groupUpdate = groupConverter.toEntiy(request);
        groupUpdate.setId(groupId);
        groupService.validateRequiredFields(groupUpdate);

        Group existingGroup = groupService.get(groupId);
        existingGroup = groupConverter.merge(groupUpdate, existingGroup);

        Group groupUpdated = groupService.update(existingGroup);

        return ResponseEntity.ok(groupConverter.toResponse(groupUpdated));
    }

    @RequireRole(roles = {USER, SUPER_ADMIN})
    @GetMapping
    @Operation(summary = "Listar todos os grupos",
            description = "Retorna uma lista de todos os grupos públicos disponíveis.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de grupos retornada com sucesso", content = @Content(schema = @Schema(implementation = GroupResponse.class))),
    })
    public ResponseEntity<List<GroupResponse>> getAll() {
        List<Group> groups = groupService.getAllPublicGroups();
        return ResponseEntity.ok(groupConverter.toResponseList(groups));
    }

    @CheckGroupMember(service = GroupMemberService.class)
    @GetMapping("/{groupId}")
    @Operation(summary = "Buscar grupo por ID",
            description = "Retorna os detalhes de um grupo específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grupo encontrado", content = @Content(schema = @Schema(implementation = GroupResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
    })
    public ResponseEntity<GroupResponse> getById(@PathVariable @GroupKey Long groupId) {
        Group group = groupService.getById(groupId);
        return ResponseEntity.ok(groupConverter.toResponse(group));
    }

    @CheckGroupMember(service = GroupMemberService.class)
    @PutMapping("/{groupId}/set-default")
    @Operation(summary = "Definir grupo como padrão",
            description = "Define um grupo como o padrão do usuário autenticado. Este será o grupo utilizado por padrão nas operações do usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Grupo definido como padrão com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é membro do grupo"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
    })
    public ResponseEntity<Void> setAsDefaultGroup(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUser().getId();
        groupMemberService.setAsDefaultGroup(userId, groupId);
        return ResponseEntity.noContent().build();
    }

    @CheckOwner(service = GroupService.class)
    @PutMapping("/{groupId}/transfer-ownership/{newOwnerId}")
    @Operation(summary = "Transferir propriedade do grupo",
            description = "Transfere a propriedade do grupo para outro membro. Apenas o owner atual pode executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propriedade transferida com sucesso", content = @Content(schema = @Schema(implementation = GroupResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para transferir propriedade"),
            @ApiResponse(responseCode = "404", description = "Grupo ou novo owner não encontrado"),
            @ApiResponse(responseCode = "422", description = "Operação inválida: novo owner é o próprio atual, não é membro ativo ou não possui o papel necessário"),
    })
    public ResponseEntity<GroupResponse> transferOwnership(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @ResourceKey Long groupId,
            @Parameter(description = "ID do novo owner", example = "1")
            @PathVariable Long newOwnerId) {
        groupService.transferOwnership(groupId, newOwnerId);
        Group group = groupService.getById(groupId);
        return ResponseEntity.ok(groupConverter.toResponse(group));
    }

    @CheckOwner(service = GroupService.class)
    @DeleteMapping("/{groupId}")
    @Operation(summary = "Deletar grupo",
            description = "Remove completamente um grupo do sistema. Apenas o owner pode executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Grupo deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para deletar este grupo"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
    })
    public ResponseEntity<Void> delete(@PathVariable @ResourceKey Long groupId) {
        groupService.deleteById(groupId);
        return ResponseEntity.noContent().build();
    }

}