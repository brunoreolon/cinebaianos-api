package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.VoteConverter;
import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieVotesResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserMovieVoteResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserVoteStatsResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.GroupKey;
import com.brunoreolon.cinebaianosapi.core.security.authorization.service.BusinessPermissionService;
import com.brunoreolon.cinebaianosapi.domain.model.MovieVotes;
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
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.*;
import static com.brunoreolon.cinebaianosapi.core.security.authorization.enums.UserRole.*;

@RestController
@RequestMapping("/api/votes")
@AllArgsConstructor
@Tag(name = "Votos", description = "Operações relacionadas ao registro, atualização e consulta de votos de filmes.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class VoteController {

    private final UserService userService;
    private final VoteService voteService;
    private final BusinessPermissionService permissionService;
    private final VoteConverter voteConverter;

    @CheckGroupMember(service = GroupMemberService.class)
    @GetMapping("/received")
    @Operation(summary = "Votos recebidos", description = "Retorna a lista de votos recebidos pelos usuários, podendo filtrar por tipo de voto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de votos recebidos retornada com sucesso", content = @Content(schema = @Schema(implementation = UserVoteStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<UserVoteStatsResponse>> getVotesReceived(
            @Parameter(description = "ID do tipo de voto (opcional)", example = "1")
            @RequestParam(name = "vote", required = false) Long voteType) {
        List<UserVoteStatsResponse> votesReceived = userService.getVotesReceived(voteType, null);
        return ResponseEntity.ok().body(votesReceived);
    }

    @RequireMinimumRole(role = USER)
    @GetMapping("/given")
    @Operation(summary = "Votos dados", description = "Retorna a lista de votos dados pelos usuários, podendo filtrar por tipo de voto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de votos dados retornada com sucesso", content = @Content(schema = @Schema(implementation = UserVoteStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<UserVoteStatsResponse>> getVotesGiven(
            @Parameter(description = "ID do tipo de voto (opcional)", example = "1")
            @RequestParam(name = "vote", required = false) Long voteType) {
        List<UserVoteStatsResponse> votesGiven = userService.getVotesGiven(voteType);
        return ResponseEntity.ok().body(votesGiven);
    }

    @RequireMinimumRole(role = USER)
    @GetMapping("/users/{userId}/movies-votes")
    @Operation(summary = "Votos de filmes de um usuário", description = "Retorna a lista de votos que um usuário deu em filmes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Votos do usuário retornados com sucesso", content = @Content(schema = @Schema(implementation = UserMovieVoteResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<UserMovieVoteResponse>> getUserMovieVotes(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId) {
        List<Vote> votes = voteService.getVotesByUser(userId);
        List<UserMovieVoteResponse> response = voteConverter.toUserMovieVoteResponseList(votes);

        return ResponseEntity.ok().body(response);
    }

//    @PostMapping
//    @RequireRole(roles = {ADMIN, USER})
//    @Operation(summary = "Registrar voto", description = "Registra um voto dado por um usuário para um filme específico.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "201", description = "Voto registrado com sucesso", content = @Content(schema = @Schema(implementation = VoteDetailResponse.class))),
//            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
//            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para registrar este voto", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
//    })
//    public ResponseEntity<VoteDetailResponse> registerVote(
//            @Parameter(description = "Detalhes do voto")
//            @Valid @RequestBody VoteRequest voteRequest) {
//        permissionService.checkCanVoteFor(voteRequest.getVoter().getId());
//
//        Vote newVote = voteService.register(voteRequest.getVoter().getId(), voteRequest.getMovie().getId(), voteRequest.getVote());
//        return ResponseEntity.status(HttpStatus.CREATED).body(voteConverter.toDetailResponse(newVote));
//    }
//
//    @PutMapping("/users/{voterId}/movies/{movieId}")
//    @CheckOwner(service = VoteService.class)
//    @Operation(summary = "Atualizar voto", description = "Atualiza o voto de um usuário para um filme específico.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "200", description = "Voto atualizado com sucesso", content = @Content(schema = @Schema(implementation = VoteDetailResponse.class))),
//            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para atualizar este voto", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
//            @ApiResponse(responseCode = "404", description = "Voto não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
//    })
//    public ResponseEntity<VoteDetailResponse> updateVote(
//            @Parameter(description = "ID do votante", example = "1")
//            @PathVariable @ResourceKey Long voterId,
//
//            @Parameter(description = "ID do filme", example = "42")
//            @PathVariable @ResourceKey Long movieId,
//
//            @Parameter(description = "Novo tipo de voto")
//            @Valid @RequestBody VoteTypeId voteTypeId) {
//        Vote newVote = voteService.update(voterId, movieId, voteTypeId.getId());
//        return ResponseEntity.ok().body(voteConverter.toDetailResponse(newVote));
//    }
//
//    @DeleteMapping("/users/{voterId}/movies/{movieId}")
//    @CheckOwner(service = VoteService.class, allowBot = true)
//    @Operation(summary = "Excluir voto", description = "Remove o voto de um usuário para um filme específico.")
//    @ApiResponses({
//            @ApiResponse(responseCode = "204", description = "Voto excluído com sucesso"),
//            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para excluir este voto", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
//            @ApiResponse(responseCode = "404", description = "Voto não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
//    })
//    public ResponseEntity<Void> deleteVote(
//            @Parameter(description = "ID do votante", example = "1")
//            @PathVariable @ResourceKey Long voterId,
//
//            @Parameter(description = "ID do filme", example = "42")
//            @PathVariable @ResourceKey Long movieId) {
//        voteService.delete(voterId, movieId);
//        return ResponseEntity.noContent().build();
//    }

    @RequireMinimumRole(role = USER)
    @GetMapping("{movieId}/votes")
    @Operation(summary = "Votos recebidos por filme", description = "Retorna a contagem de votos recebidos por um filme específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contagem de votos retornada com sucesso", content = @Content(schema = @Schema(implementation = MovieVotesResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<MovieVotesResponse> getMovieVotesReceived(
            @Parameter(description = "ID do filme", example = "42")
            @PathVariable Long movieId) {
        MovieVotes movieVotesReceived = voteService.getMovieVotesReceived(movieId);
        MovieVotesResponse movieVotesResponse = voteConverter.toMovieVotesResponse(movieVotesReceived);

        return ResponseEntity.ok().body(movieVotesResponse);
    }

}