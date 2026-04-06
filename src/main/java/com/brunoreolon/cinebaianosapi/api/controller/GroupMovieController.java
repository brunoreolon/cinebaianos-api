package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.GroupConverter;
import com.brunoreolon.cinebaianosapi.api.converter.TmdbConverter;
import com.brunoreolon.cinebaianosapi.api.model.group.response.GroupDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.request.MovieIdRequest;
import com.brunoreolon.cinebaianosapi.api.model.movie.request.MovieSearchRequest;
import com.brunoreolon.cinebaianosapi.api.model.tmdb.TmdbMovieResponse;
import com.brunoreolon.cinebaianosapi.client.TmdbProperties;
import com.brunoreolon.cinebaianosapi.client.model.ClientMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.client.model.ClientResultsResponse;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupMember;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.GroupKey;
import com.brunoreolon.cinebaianosapi.domain.exception.MultipleMoviesFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMemberService;
import com.brunoreolon.cinebaianosapi.domain.service.GroupMovieService;
import com.brunoreolon.cinebaianosapi.domain.service.GroupService;
import com.brunoreolon.cinebaianosapi.domain.service.TmdbService;
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
@RequestMapping("/api/groups/{groupId}/movies")
@Tag(name = "Filmes em Grupos", description = "Operações relacionadas ao gerenciamento de filmes em grupos.")
public class GroupMovieController {

    private final GroupMovieService groupMovieService;
    private final GroupMemberService groupMemberService;
    private final GroupService groupService;
    private final TmdbService tmdbService;
    private final GroupConverter groupConverter;
    private final TmdbConverter tmdbConverter;
    private final TmdbProperties tmdbProperties;

    @CheckGroupMember(service = GroupMemberService.class)
    @GetMapping
    @Operation(summary = "Obter filmes do grupo",
            description = "Retorna todos os filmes adicionados a um grupo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filmes encontrados", content = @Content(schema = @Schema(implementation = GroupDetailResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não é membro do grupo"),
            @ApiResponse(responseCode = "404", description = "Grupo não encontrado"),
    })
    public ResponseEntity<GroupDetailResponse> getGroupMovies(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId) {
        Group group = groupService.getGroupWithMovies(groupId);
        GroupDetailResponse response = groupConverter.toGroupWithMoviesResponse(group);
        return ResponseEntity.ok(response);
    }

    @CheckGroupMember(service = GroupMemberService.class, allowBot = true)
    @PostMapping()
    @Operation(summary = "Adicionar filme ao grupo pelo TMDb ID",
            description = "Adiciona um filme ao grupo usando o TMDb ID. Se o filme ainda não existe no sistema, ele será criado globalmente e vinculado ao grupo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Filme adicionado com sucesso", content = @Content(schema = @Schema(implementation = GroupDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é membro do grupo ou não pode adicionar filmes"),
            @ApiResponse(responseCode = "404", description = "Grupo ou filme não encontrado no TMDb"),
            @ApiResponse(responseCode = "409", description = "Filme já foi adicionado a este grupo"),
            @ApiResponse(responseCode = "422", description = "Operação inválida: apenas administradores podem adicionar filmes neste grupo"),
    })
    public ResponseEntity<GroupDetailResponse> addMovieByTmdbId(
            @Valid @RequestBody MovieIdRequest movieIdRequest,
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "Idioma para buscar dados no TMDb", example = "pt-BR")
            @RequestParam(name = "language", required = false) String language) {

        Long chooserId = movieIdRequest.getChooser().getId();
        groupMemberService.validateMember(groupId, chooserId);

        if (language == null) {
            language = tmdbProperties.getLanguage();
        }

        ClientMovieDetailsResponse movieDetails = tmdbService.getMovieDetails(movieIdRequest.getMovie().getId(), language);
        Movie movie = tmdbConverter.toEntityFromClientMovieDetail(movieDetails);

        groupMovieService.addMovieToGroup(groupId, movie, chooserId);

        Group updatedGroup = groupService.getGroupWithMovies(groupId);
        GroupDetailResponse response = groupConverter.toGroupWithMoviesResponse(updatedGroup);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @CheckGroupMember(service = GroupMemberService.class, allowBot = true)
    @PostMapping("/search")
    @Operation(summary = "Buscar e adicionar filme ao grupo por título e ano",
            description = """
                    Realiza uma busca no TMDb utilizando título e ano.
                    - Se apenas um resultado for encontrado, o filme será adicionado automaticamente ao grupo.
                    - Se múltiplos resultados forem encontrados, a requisição falha com status 409,
                      retornando as opções encontradas.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Filme adicionado com sucesso", content = @Content(schema = @Schema(implementation = GroupDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é membro do grupo ou não pode adicionar filmes"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado no TMDb"),
            @ApiResponse(responseCode = "409", description = "Conflito: múltiplos filmes encontrados na busca ou já adicionado ao grupo"),
            @ApiResponse(responseCode = "422", description = "Operação inválida: apenas administradores podem adicionar filmes neste grupo"),
    })
    public ResponseEntity<GroupDetailResponse> searchAndAddMovie(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Valid @RequestBody MovieSearchRequest movieSearchRequest,
            @Parameter(description = "Idioma para buscar dados no TMDb", example = "pt-BR")
            @RequestParam(name = "language", required = false) String language) {

        Long chooserId = movieSearchRequest.getChooser().getId();
        groupMemberService.validateMember(groupId, chooserId);

        if (language == null) {
            language = tmdbProperties.getLanguage();
        }

        ClientResultsResponse response = tmdbService.search(
                movieSearchRequest.getTitle(),
                movieSearchRequest.getYear(),
                language
        );
        List<TmdbMovieResponse> tmdbMovieResponses = tmdbConverter.toMovieResponseList(response.getResults());

        if (tmdbMovieResponses.size() > 1) {
            throw new MultipleMoviesFoundException(tmdbMovieResponses);
        }

        ClientMovieDetailsResponse movieDetails = tmdbService.getMovieDetails(
                tmdbMovieResponses.getFirst().getId(),
                language
        );
        Movie movie = tmdbConverter.toEntityFromClientMovieDetail(movieDetails);

        groupMovieService.addMovieToGroup(groupId, movie, chooserId);

        Group updatedGroup = groupService.getGroupWithMovies(groupId);
        GroupDetailResponse groupResponse = groupConverter.toGroupWithMoviesResponse(updatedGroup);

        return ResponseEntity.status(HttpStatus.CREATED).body(groupResponse);
    }

    @CheckGroupMember(service = GroupMemberService.class)
    @DeleteMapping("/{movieId}")
    @Operation(summary = "Remover filme do grupo",
            description = "Remove um filme do grupo. Apenas o usuário que adicionou o filme ou um admin do grupo podem remover.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Filme removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é membro do grupo ou não tem permissão para remover"),
            @ApiResponse(responseCode = "404", description = "Grupo ou filme não encontrado"),
            @ApiResponse(responseCode = "422", description = "Operação inválida: apenas quem adicionou ou admin podem remover"),
    })
    public ResponseEntity<Void> removeMovieFromGroup(
            @Parameter(description = "ID do grupo", example = "1")
            @PathVariable @GroupKey Long groupId,
            @Parameter(description = "ID do filme", example = "1")
            @PathVariable Long movieId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        groupMovieService.removeMovieFromGroup(groupId, movieId, userDetails.getUser().getId());

        return ResponseEntity.noContent().build();
    }
}