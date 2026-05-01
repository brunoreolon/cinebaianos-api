package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.VoteTypeConverter;
import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.VoteTypeUpdateRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteTypeDetailResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupMember;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.GroupKey;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.domain.model.VoteType;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMemberService;
import com.brunoreolon.cinebaianosapi.domain.service.VoteTypeRegistrationService;
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
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class GroupVoteTypeController {

    private final VoteTypeRegistrationService voteTypeRegistrationService;
    private final VoteTypeConverter voteTypeConverter;

    @CheckGroupMember(service = GroupMemberService.class)
    @GetMapping
    @Operation(summary = "Listar tipos de voto do grupo", description = "Retorna os tipos de voto locais ativos cadastrados para o grupo informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipos de voto do grupo retornados com sucesso", content = @Content(schema = @Schema(implementation = VoteTypeDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não é membro do grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<VoteTypeDetailResponse>> listGroupVoteTypes(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId) {
        List<VoteType> votes = voteTypeRegistrationService.getAllByGroup(groupId, true);
        return ResponseEntity.ok(voteTypeConverter.toDetailResponseList(votes));
    }

    @CheckGroupMember(service = GroupMemberService.class)
    @GetMapping("/global")
    @Operation(summary = "Listar tipos de voto globais", description = "Retorna os tipos de voto globais ativos visíveis para membros do grupo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipos de voto globais retornados com sucesso", content = @Content(schema = @Schema(implementation = VoteTypeDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não é membro do grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<VoteTypeDetailResponse>> listGlobalVoteTypes(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId) {
        List<VoteType> votes = voteTypeRegistrationService.getAllGlobal(true);
        return ResponseEntity.ok(voteTypeConverter.toDetailResponseList(votes));
    }

    @CheckGroupMember(service = GroupMemberService.class)
    @GetMapping("/available")
    @Operation(summary = "Listar tipos de voto disponíveis para votação no grupo", description = "Retorna os tipos de voto que podem ser utilizados em votos do grupo: tipos locais ativos e, quando permitido, tipos globais ativos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipos de voto disponíveis retornados com sucesso", content = @Content(schema = @Schema(implementation = VoteTypeDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não é membro do grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<VoteTypeDetailResponse>> listAvailableVoteTypes(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId) {
        List<VoteType> votes = voteTypeRegistrationService.getAvailableByGroupForVoting(groupId);
        return ResponseEntity.ok(voteTypeConverter.toDetailResponseList(votes));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PostMapping
    @Operation(summary = "Criar tipo de voto do grupo", description = "Cria um novo tipo de voto local no contexto do grupo informado. Apenas administradores do grupo podem executar esta operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tipo de voto do grupo criado com sucesso", content = @Content(schema = @Schema(implementation = VoteTypeDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para criar tipo de voto no grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe tipo de voto conflitante com este nome", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<VoteTypeDetailResponse> createGroupVoteType(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "Dados do tipo de voto a ser criado no grupo")
            @Valid @RequestBody VoteTypeRequest voteTypeRequest) {
        VoteType voteType = voteTypeConverter.toEntityFromCreate(voteTypeRequest);
        VoteType created = voteTypeRegistrationService.createByGroup(groupId, voteType);
        return ResponseEntity.status(HttpStatus.CREATED).body(voteTypeConverter.toDetailResponse(created));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @PutMapping("/{typeVoteId}")
    @Operation(summary = "Atualizar tipo de voto do grupo", description = "Atualiza um tipo de voto local pertencente ao grupo informado. Apenas administradores do grupo podem executar esta operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipo de voto do grupo atualizado com sucesso", content = @Content(schema = @Schema(implementation = VoteTypeDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para atualizar tipo de voto no grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo ou tipo de voto não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe tipo de voto conflitante com este nome", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Tipo de voto não pertence ao grupo informado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<VoteTypeDetailResponse> updateGroupVoteType(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "ID do tipo de voto do grupo", example = "10")
            @PathVariable Long typeVoteId,
            @Parameter(description = "Dados atualizados do tipo de voto do grupo")
            @Valid @RequestBody VoteTypeUpdateRequest voteTypeUpdateRequest) {
        VoteType input = voteTypeConverter.toEntityFromUpdate(voteTypeUpdateRequest);
        VoteType updated = voteTypeRegistrationService.updateByGroup(groupId, typeVoteId, input);
        return ResponseEntity.ok(voteTypeConverter.toDetailResponse(updated));
    }

    @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.ADMIN)
    @DeleteMapping("/{typeVoteId}")
    @Operation(summary = "Excluir tipo de voto do grupo", description = "Remove um tipo de voto local do grupo informado. Apenas administradores do grupo podem executar esta operação.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tipo de voto do grupo excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para excluir tipo de voto no grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo ou tipo de voto não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Tipo de voto está em uso e não pode ser removido", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Tipo de voto não pertence ao grupo informado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteGroupVoteType(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "ID do tipo de voto do grupo", example = "10")
            @PathVariable Long typeVoteId) {
        voteTypeRegistrationService.deleteByGroup(groupId, typeVoteId);
        return ResponseEntity.noContent().build();
    }

}