package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.VoteConverter;
import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.id.VoteTypeId;
import com.brunoreolon.cinebaianosapi.api.model.vote.request.GroupVoteRequest;
import com.brunoreolon.cinebaianosapi.api.model.vote.response.VoteDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserVoteStatsResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupMember;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.GroupKey;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.ResourceKey;
import com.brunoreolon.cinebaianosapi.core.security.authorization.service.BusinessPermissionService;
import com.brunoreolon.cinebaianosapi.domain.model.Vote;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMemberService;
import com.brunoreolon.cinebaianosapi.domain.service.UserService;
import com.brunoreolon.cinebaianosapi.domain.service.VoteService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/votes")
@AllArgsConstructor
@Tag(name = "Votos de Grupo", description = "Operações relacionadas ao registro, atualização e consulta de votos dentro de grupos específicos.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class GroupVoteController {

    private final VoteService voteService;
    private final BusinessPermissionService permissionService;
    private final VoteConverter voteConverter;
    private final UserService userService;

    @CheckGroupMember(service = GroupMemberService.class, allowBot = true)
    @PostMapping
    @Operation(summary = "Registrar voto no grupo", description = "Registra um voto de um usuário em um filme específico dentro do grupo informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Voto registrado com sucesso", content = @Content(schema = @Schema(implementation = VoteDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para registrar este voto", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo, filme ou tipo de voto não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Voto já existe para este filme neste grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Tipo de voto não é válido para o grupo informado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<VoteDetailResponse> registerGroupVote(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,

            @Parameter(description = "Detalhes do voto para o grupo")
            @Valid @RequestBody GroupVoteRequest voteRequest) {

        permissionService.checkCanVoteFor(voteRequest.getVoter().getId());

        Vote newVote = voteService.registerByGroup(
                voteRequest.getVoter().getId(),
                groupId,
                voteRequest.getMovie().getId(),
                voteRequest.getVote()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(voteConverter.toDetailResponse(newVote));
    }

    @CheckGroupMember(service = GroupMemberService.class, allowBot = true)
    @PutMapping("/users/{voterId}/movies/{movieId}")
    @Operation(summary = "Atualizar voto no grupo", description = "Atualiza o voto de um usuário para um filme específico dentro do grupo informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Voto atualizado com sucesso", content = @Content(schema = @Schema(implementation = VoteDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para atualizar este voto", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo, filme, voto ou tipo de voto não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Prazo para modificação de voto expirado ou tipo de voto não é válido para o grupo", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<VoteDetailResponse> updateGroupVote(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,

            @Parameter(description = "ID do votante", example = "1")
            @PathVariable @ResourceKey Long voterId,

            @Parameter(description = "ID do filme", example = "42")
            @PathVariable @ResourceKey Long movieId,

            @Parameter(description = "Novo tipo de voto")
            @Valid @RequestBody VoteTypeId voteTypeId) {

        permissionService.checkCanVoteFor(voterId);
        Vote updatedVote = voteService.updateByGroup(voterId, groupId, movieId, voteTypeId.getId());
        return ResponseEntity.ok().body(voteConverter.toDetailResponse(updatedVote));
    }

    @CheckGroupMember(service = GroupMemberService.class, allowBot = true)
    @DeleteMapping("/users/{voterId}/movies/{movieId}")
    @Operation(summary = "Excluir voto do grupo", description = "Remove o voto de um usuário para um filme específico dentro do grupo informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Voto excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para excluir este voto", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Grupo, filme ou voto não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "Operação inválida para o voto informado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> deleteGroupVote(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,

            @Parameter(description = "ID do votante", example = "1")
            @PathVariable @ResourceKey Long voterId,

            @Parameter(description = "ID do filme", example = "42")
            @PathVariable @ResourceKey Long movieId) {

        permissionService.checkCanRemoveVoteFor(voterId);
        voteService.deleteByGroup(voterId, groupId, movieId);
        return ResponseEntity.noContent().build();
    }

    @CheckGroupMember(service = GroupMemberService.class)
    @GetMapping("/received")
    @Operation(summary = "Votos recebidos no grupo", description = "Retorna a lista de votos recebidos pelos usuários do grupo, podendo filtrar por tipo de voto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de votos recebidos retornada com sucesso", content = @Content(schema = @Schema(implementation = UserVoteStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<UserVoteStatsResponse>> getVotesReceivedByGroup(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "ID do tipo de voto (opcional)", example = "1")
            @RequestParam(name = "vote", required = false) Long voteType) {
        List<UserVoteStatsResponse> votesReceived = userService.getVotesReceived(voteType, groupId);
        return ResponseEntity.ok().body(votesReceived);
    }

}