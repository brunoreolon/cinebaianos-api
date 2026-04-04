package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserSummaryResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.stats.UserVoteStatsResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.domain.model.Role;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.service.UserRegistratioService;
import com.brunoreolon.cinebaianosapi.domain.service.UserService;
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

import static com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Tag(name = "Estatísticas de Usuário", description = "Operações relacionadas a estatísticas de votos e resumo de usuários.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class UserStatsController {

    private final UserService userService;
    private final UserRegistratioService userRegistratioService;

    @GetMapping("/{userId}/votes/received")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(summary = "Votos recebidos por usuário", description = "Retorna estatísticas de votos recebidos por um usuário, podendo filtrar por tipo de voto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas de votos recebidos retornadas com sucesso", content = @Content(schema = @Schema(implementation = UserVoteStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserVoteStatsResponse> getVotesReceivedByUser(
            @Parameter(description = "ID do usuário", example = "987654321098765432")
            @PathVariable Long userId,

            @Parameter(description = "ID do tipo de voto (opcional)", example = "1")
            @RequestParam(name = "vote", required = false) Long voteType) {
        User user = userRegistratioService.get(userId);
        UserVoteStatsResponse votesReceived = userService.getVotesReceivedByUser(user, voteType);

        return ResponseEntity.ok().body(votesReceived);
    }

    @GetMapping("/{userId}/votes/given")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(summary = "Votos dados por usuário", description = "Retorna estatísticas de votos dados por um usuário, podendo filtrar por tipo de voto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estatísticas de votos dados retornadas com sucesso", content = @Content(schema = @Schema(implementation = UserVoteStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserVoteStatsResponse> getVotesGivenByUser(
            @Parameter(description = "ID do usuário", example = "1")
            @PathVariable Long userId,

            @Parameter(description = "ID do tipo de voto (opcional)", example = "1")
            @RequestParam(name = "vote", required = false) Long voteType) {
        User user = userRegistratioService.get(userId);
        UserVoteStatsResponse votesGiven = userService.getVotesGivenByUser(user, voteType);

        return ResponseEntity.ok().body(votesGiven);
    }

    @GetMapping("/{userId}/summary")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(summary = "Resumo do usuário", description = "Retorna um resumo completo de estatísticas do usuário, incluindo votos e métricas gerais.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resumo do usuário retornado com sucesso", content = @Content(schema = @Schema(implementation = UserSummaryResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<UserSummaryResponse> getUserSummary(
            @Parameter(description = "ID do usuário", example = "987654321098765432")
            @PathVariable Long userId) {
        UserSummaryResponse userSummaryStats = userService.getUserSummary(userId);

        return ResponseEntity.ok().body(userSummaryStats);
    }

}