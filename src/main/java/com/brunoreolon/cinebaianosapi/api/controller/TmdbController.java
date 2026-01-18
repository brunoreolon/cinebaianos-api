package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.TmdbConverter;
import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.tmdb.TmdbMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.api.model.tmdb.TmdbMovieResponse;
import com.brunoreolon.cinebaianosapi.client.TmdbProperties;
import com.brunoreolon.cinebaianosapi.client.model.ClientMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.client.model.ClientResultsResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.domain.model.Role;
import com.brunoreolon.cinebaianosapi.domain.service.TmdbService;
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

@RestController
@RequestMapping("/api/tmdb")
@AllArgsConstructor
@Tag(name = "TMDb", description = "Operações de busca e consulta de filmes utilizando a API do TMDb.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class TmdbController {

    private final TmdbService tmdbService;
    private final TmdbConverter tmdbConverter;
    private final TmdbProperties tmdbProperties;

    @GetMapping("/search/movies-details")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Buscar filmes com os detalhes pelo título",
            description = "Realiza uma busca de filmes no TMDb usando o título e, opcionalmente, o ano de lançamento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filmes encontrados com sucesso", content = @Content(schema = @Schema(implementation = TmdbMovieResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum filme encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<TmdbMovieDetailsResponse>> searchWithDetails(
            @Parameter(description = "Título do filme a ser buscado", required = true, example = "Matrix")
            @RequestParam(name = "title") String title,

            @Parameter(description = "Ano de lançamento do filme", required = false, example = "1999")
            @RequestParam(name = "year", required = false) String year,

            @Parameter(description = "Idioma da busca (opcional, padrão do sistema será usado se não informado)", example = "pt-BR")
            @RequestParam(name = "language", required = false) String language) {

        if (language == null) {
            language = tmdbProperties.getLanguage();
        }

        List<ClientMovieDetailsResponse> response = tmdbService.searchWithDetail(title, year, language);
        List<TmdbMovieDetailsResponse> movieDetailsResponseList = tmdbConverter.toMovieDetailsResponseList(response);

        return ResponseEntity.ok().body(movieDetailsResponseList);
    }

    @GetMapping("/search/movies")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Buscar filmes pelo título",
            description = "Realiza uma busca de filmes no TMDb usando o título e, opcionalmente, o ano de lançamento."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filmes encontrados com sucesso", content = @Content(schema = @Schema(implementation = TmdbMovieResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum filme encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<List<TmdbMovieResponse>> search(
            @Parameter(description = "Título do filme a ser buscado", required = true, example = "Matrix")
            @RequestParam(name = "title") String title,

            @Parameter(description = "Ano de lançamento do filme", required = false, example = "1999")
            @RequestParam(name = "year", required = false) String year,

            @Parameter(description = "Idioma da busca (opcional, padrão do sistema será usado se não informado)", example = "pt-BR")
            @RequestParam(name = "language", required = false) String language) {

        if (language == null) {
            language = tmdbProperties.getLanguage();
        }

        ClientResultsResponse response = tmdbService.search(title, year, language);
        return ResponseEntity.ok().body(tmdbConverter.toMovieResponseList(response.getResults()));
    }

    @GetMapping("/movies/{movieId}")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Buscar detalhes de filme pelo ID",
            description = "Retorna informações detalhadas de um filme do TMDb a partir do seu ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalhes do filme retornados com sucesso", content = @Content(schema = @Schema(implementation = TmdbMovieDetailsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<TmdbMovieDetailsResponse> searchDetails(
            @Parameter(description = "ID do filme no TMDb", required = true, example = "603")
            @PathVariable("movieId") Long movieId,

            @Parameter(description = "Idioma da consulta (opcional, padrão do sistema será usado se não informado)", example = "pt-BR")
            @RequestParam(name = "language", required = false) String language) {

        if (language == null) {
            language = tmdbProperties.getLanguage();
        }

        ClientMovieDetailsResponse movieDetails = tmdbService.getMovieDetails(movieId, language);
        return ResponseEntity.ok().body(tmdbConverter.toMovieDetailsResponse(movieDetails));
    }
}