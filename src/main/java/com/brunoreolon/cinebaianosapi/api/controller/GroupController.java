package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.GroupConverter;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.request.GroupUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupDetailWithMembersResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupMemberResponse;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupResponse;
import com.brunoreolon.cinebaianosapi.domain.model.CustomUserDetails;
import com.brunoreolon.cinebaianosapi.domain.model.Group;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMember;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMemberService;
import com.brunoreolon.cinebaianosapi.domain.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/groups")
@Tag(name = "Grupos", description = "Operações relacionadas ao gerenciamento de grupos de usuários e membros.")
public class GroupController {

    private final GroupService groupService;
    private final GroupMemberService groupMemberService;
    private final GroupConverter groupConverter;

    @PostMapping
    @Operation(summary = "Criar novo grupo",
            description = "Cria um novo grupo. O usuário autenticado será definido como o owner do grupo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Grupo criado com sucesso", content = @Content(schema = @Schema(implementation = GroupResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
    })
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        Group group = groupConverter.toEntiy(request);

        Long ownerId = userDetails.getUser().getId();

        Group newGroup = groupService.save(group, ownerId);

        return ResponseEntity.status(HttpStatus.CREATED).body(groupConverter.toResponse(newGroup));
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "Atualizar grupo",
            description = "Atualiza as configurações de um grupo existente. Apenas o owner ou admin do grupo podem executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grupo atualizado com sucesso", content = @Content(schema = @Schema(implementation = GroupResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para atualizar este grupo"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
    })
    public ResponseEntity<GroupResponse> update(@PathVariable Long groupId, @Valid @RequestBody GroupUpdateRequest request) {
        Group group = groupConverter.toEntiy(request);
        group.setId(groupId);
        Group updatedGroup = groupService.update(group);

        return ResponseEntity.ok(groupConverter.toResponse(updatedGroup));
    }

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

    @GetMapping("/{groupId}")
    @Operation(summary = "Buscar grupo por ID",
            description = "Retorna os detalhes de um grupo específico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grupo encontrado", content = @Content(schema = @Schema(implementation = GroupResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
    })
    public ResponseEntity<GroupResponse> getById(@PathVariable("groupId") Long groupId) {
        Group group = groupService.getById(groupId);
        return ResponseEntity.ok(groupConverter.toResponse(group));
    }

    @GetMapping("/{groupId}/movies")
    @Operation(summary = "Obter grupo com filmes",
            description = "Retorna um grupo com todos os seus filmes associados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Grupo com filmes encontrado", content = @Content(schema = @Schema(implementation = GroupDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
    })
    public ResponseEntity<GroupDetailResponse> getGroupWithMovies(@PathVariable Long groupId) {
        try {
            Group group = groupService.getGroupWithMovies(groupId);
            GroupDetailResponse groupResponse = groupConverter.toGroupWithMoviesResponse(group);

            return ResponseEntity.ok(groupResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{groupId}/members")
    @Operation(summary = "Obter membros do grupo",
            description = "Retorna um grupo com todos os seus membros ativos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membros do grupo encontrados", content = @Content(schema = @Schema(implementation = GroupDetailWithMembersResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
    })
    public ResponseEntity<GroupDetailWithMembersResponse> getGroupWithMembers(@PathVariable Long groupId) {
        try {
            Group group = groupService.getGroupMembers(groupId);
            GroupDetailWithMembersResponse response = groupConverter.toGroupWithMembersResponse(group);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

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
            @PathVariable Long groupId,
            @Parameter(description = "ID do usuário a ser adicionado", example = "1")
            @PathVariable Long userId) {
        try {
            GroupMember member = groupMemberService.addMember(groupId, userId, GroupMemberRole.MEMBER);
            return ResponseEntity.ok(groupConverter.toMemberResponse(member));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("já é membro")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @Operation(summary = "Remover membro do grupo",
            description = "Remove um membro do grupo. O membro será marcado como inativo e seus dados permanecerão no sistema. Apenas o owner ou admin podem executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Membro removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para remover membros"),
            @ApiResponse(responseCode = "404", description = "Grupo ou membro não encontrado"),
    })
    public ResponseEntity<Void> removeMember(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "ID do usuário a ser removido", example = "1")
            @PathVariable Long userId) {
        try {
            groupMemberService.removeMember(groupId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{groupId}/members/{userId}/promote-to-admin")
    @Operation(summary = "Promover membro para admin",
            description = "Promove um membro para admin do grupo. Apenas o owner pode executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Membro promovido com sucesso", content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para promover membros"),
            @ApiResponse(responseCode = "404", description = "Grupo ou membro não encontrado"),
    })
    public ResponseEntity<GroupMemberResponse> promoteToAdmin(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "ID do usuário a ser promovido", example = "1")
            @PathVariable Long userId) {
        try {
            groupMemberService.promoteToAdmin(groupId, userId);
            GroupMember member = groupMemberService.getMember(groupId, userId)
                    .orElseThrow(() -> new RuntimeException("Membro não encontrado"));
            return ResponseEntity.ok(groupConverter.toMemberResponse(member));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{groupId}/members/{userId}/demote-to-member")
    @Operation(summary = "Rebaixar admin para membro",
            description = "Rebaixa um admin para membro do grupo. Apenas o owner pode executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin rebaixado com sucesso", content = @Content(schema = @Schema(implementation = GroupMemberResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para rebaixar membros"),
            @ApiResponse(responseCode = "404", description = "Grupo ou membro não encontrado"),
    })
    public ResponseEntity<GroupMemberResponse> demoteToMember(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "ID do admin a ser rebaixado", example = "1")
            @PathVariable Long userId) {
        try {
            groupMemberService.demoteToMember(groupId, userId);
            GroupMember member = groupMemberService.getMember(groupId, userId)
                    .orElseThrow(() -> new RuntimeException("Membro não encontrado"));
            return ResponseEntity.ok(groupConverter.toMemberResponse(member));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{groupId}/set-default")
    @Operation(summary = "Definir grupo como padrão",
            description = "Define um grupo como o padrão do usuário autenticado. Este será o grupo utilizado por padrão nas operações do usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Grupo definido como padrão com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
    })
    public ResponseEntity<Void> setAsDefaultGroup(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable Long groupId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Long userId = userDetails.getUser().getId();
            groupMemberService.setAsDefaultGroup(userId, groupId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{groupId}/transfer-ownership/{newOwnerId}")
    @Operation(summary = "Transferir propriedade do grupo",
            description = "Transfere a propriedade do grupo para outro membro. Apenas o owner atual pode executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Propriedade transferida com sucesso", content = @Content(schema = @Schema(implementation = GroupResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para transferir propriedade"),
            @ApiResponse(responseCode = "404", description = "Grupo ou novo owner não encontrado"),
    })
    public ResponseEntity<GroupResponse> transferOwnership(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "ID do novo owner", example = "1")
            @PathVariable Long newOwnerId) {
        try {
            groupService.transferOwnership(groupId, newOwnerId);
            Group group = groupService.getById(groupId);
            return ResponseEntity.ok(groupConverter.toResponse(group));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "Deletar grupo",
            description = "Remove completamente um grupo do sistema. Apenas o owner pode executar esta operação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Grupo deletado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para deletar este grupo"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
    })
    public ResponseEntity<Void> delete(@PathVariable Long groupId) {
        try {
            groupService.deleteById(groupId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
