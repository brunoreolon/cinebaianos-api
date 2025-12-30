package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.GenreConverter;
import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.genre.GenreResponse;
import com.brunoreolon.cinebaianosapi.api.model.genre.stats.GenreStatsResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.stats.GenreVoteBreakdownResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.domain.model.Genre;
import com.brunoreolon.cinebaianosapi.domain.model.Role;
import com.brunoreolon.cinebaianosapi.domain.service.GenreService;
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
import java.util.Map;

import static com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.*;

@RestController
@RequestMapping("/api/genres")
@AllArgsConstructor
@Tag(name = "Gêneros", description = "Operações relacionadas a gêneros de filmes, incluindo rankings e contagem de votos.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class GenreController {

    private final GenreService genreService;
    private final GenreConverter genreConverter;

    @GetMapping("/rankings")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Ranking de gêneros",
            description = "Retorna a lista de gêneros ordenada pelo total de filmes cadastrados em cada gênero."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking retornado com sucesso", content = @Content(schema = @Schema(implementation = GenreStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<GenreStatsResponse>> getGenreRankings() {
        Map<String, Integer> genreRankings = genreService.getGenreRankings();
        return ResponseEntity.ok().body(genreConverter.toResponseList(genreRankings));
    }

    @GetMapping("/vote-counts")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Contagem de votos por gênero",
            description = "Retorna a contagem detalhada de votos por gênero. Pode ser filtrado por um tipo de voto específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contagem de votos retornada com sucesso", content = @Content(schema = @Schema(implementation = GenreVoteBreakdownResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<GenreVoteBreakdownResponse>> getGenreVoteBreakdown(
            @Parameter(description = "ID do tipo de voto para filtrar (opcional)", example = "1")
            @RequestParam(name = "type", required = false) Long voteTypeId) {
        List<GenreVoteBreakdownResponse> genreVoteBreakdown = genreService.getGenreVoteBreakdown(voteTypeId);
        return ResponseEntity.ok().body(genreVoteBreakdown);
    }

    @GetMapping("/users/{discordId}")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Gêneros por usuário",
            description = "Retorna a contagem de filmes por gênero cadastrados por um usuário específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gêneros retornados com sucesso", content = @Content(schema = @Schema(implementation = GenreStatsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<GenreStatsResponse>> getGenresByUser(
            @Parameter(description = "Discord ID do usuário", example = "987654321098765432")
            @PathVariable String discordId) {
        Map<String, Integer> genreCount = genreService.getGenreRankingsByUser(discordId);
        return ResponseEntity.ok().body(genreConverter.toResponseList(genreCount));
    }

    @GetMapping()
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Listar todos os gêneros",
            description = "Retorna a lista completa de gêneros cadastrados no sistema."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gêneros retornados com sucesso", content = @Content(schema = @Schema(implementation = GenreResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<GenreResponse>> getAllGenres() {
        List<Genre> genres = genreService.getAllGenres();
        return ResponseEntity.ok().body(genreConverter.toDetailResponseList(genres));
    }

}