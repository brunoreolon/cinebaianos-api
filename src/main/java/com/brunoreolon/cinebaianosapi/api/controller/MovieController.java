package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.MovieConverter;
import com.brunoreolon.cinebaianosapi.api.converter.TmdbConverter;
import com.brunoreolon.cinebaianosapi.api.converter.UserConverter;
import com.brunoreolon.cinebaianosapi.api.model.ApiErrorResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.MoviePage;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieVoteDetailResponse;
import com.brunoreolon.cinebaianosapi.api.model.movie.request.MovieIdRequest;
import com.brunoreolon.cinebaianosapi.api.model.movie.request.MovieSearchRequest;
import com.brunoreolon.cinebaianosapi.api.model.movie.response.MovieWithChooserResponse;
import com.brunoreolon.cinebaianosapi.api.model.tmdb.TmdbMovieResponse;
import com.brunoreolon.cinebaianosapi.api.model.user.response.UserWithMoviesResponse;
import com.brunoreolon.cinebaianosapi.api.model.vote.id.VoteTypeId;
import com.brunoreolon.cinebaianosapi.api.queryFilter.MovieQueryFilter;
import com.brunoreolon.cinebaianosapi.client.TmdbProperties;
import com.brunoreolon.cinebaianosapi.client.model.ClientMovieDetailsResponse;
import com.brunoreolon.cinebaianosapi.client.model.ClientResultsResponse;
import com.brunoreolon.cinebaianosapi.core.security.authentication.SecurityConfig;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.ResourceKey;
import com.brunoreolon.cinebaianosapi.core.security.authorization.service.BusinessPermissionService;
import com.brunoreolon.cinebaianosapi.domain.exception.MultipleMoviesFoundException;
import com.brunoreolon.cinebaianosapi.domain.model.*;
import com.brunoreolon.cinebaianosapi.domain.service.MovieService;
import com.brunoreolon.cinebaianosapi.domain.service.TmdbService;
import com.brunoreolon.cinebaianosapi.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.*;

@RestController
@RequestMapping("/api/movies")
@AllArgsConstructor
@Tag(name = "Filmes", description = "Operações relacionadas ao gerenciamento de filmes assistidos, incluindo cadastro, consulta e exclusão.")
@SecurityRequirement(name = SecurityConfig.SECURITY)
public class MovieController {

    private final MovieService movieService;
    private final TmdbService tmdbService;
    private final UserService userService;
    private final BusinessPermissionService permissionService;
    private final MovieConverter movieConverter;
    private final TmdbConverter tmdbConverter;
    private final UserConverter userConverter;
    private final TmdbProperties tmdbProperties;

    @PostMapping
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(summary = "Adicionar filme pelo TMDb ID",
            description = "Recebe o TMDb ID de um filme, obtém os dados diretamente da API do TMDb e realiza o cadastro no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Filme cadastrado com sucesso", content = @Content(schema = @Schema(implementation = MovieWithChooserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para adicionar filmes para outro usuário", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado no TMDb", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
    })
    public ResponseEntity<MovieWithChooserResponse> addById(
            @Valid @RequestBody MovieIdRequest movieIdRequest,

            @Parameter(
                    description = "Idioma usado nas consultas ao TMDb. Se não informado, utiliza o idioma padrão do sistema.",
                    example = "pt-BR"
            )
            @RequestParam(name = "language", required = false) String language) {
        if (language == null) {
            language = tmdbProperties.getLanguage();
        }

        permissionService.checkCanAddMovieFor(movieIdRequest.getChooser().getDiscordId());

        ClientMovieDetailsResponse movieDetails = tmdbService.getMovieDetails(movieIdRequest.getMovie().getId(), language);
        Movie movie = tmdbConverter.toEntityFromClientMovieDetail(movieDetails);
        Long voteId = getVoteId(movieIdRequest.getVote());
        Movie newMovie = movieService.save(movie, movieIdRequest.getChooser().getDiscordId(), voteId);

        return ResponseEntity.status(HttpStatus.CREATED).body(movieConverter.toWithChooserResponse(newMovie));
    }

    @PostMapping("/candidates")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(summary = "Adicionar filme informando título e ano",
            description = """
                    Realiza uma busca no TMDb utilizando título e ano.
                    - Se apenas um resultado for encontrado, o filme será cadastrado automaticamente.
                    - Se múltiplos resultados forem encontrados, a requisição falha com status 409,
                      retornando as opções encontradas.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Filme cadastrado com sucesso", content = @Content(schema = @Schema(implementation = MovieVoteDetailResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para adicionar filmes para outro usuário", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado no TMDb", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflito de negócio (ex: múltiplos filmes encontrados ao pesquisar pelo título/ano)", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
    })
    public ResponseEntity<MovieVoteDetailResponse> searchAndAddMovie(
            @Valid @RequestBody MovieSearchRequest movieSearchRequest,

            @Parameter(
                    description = "Idioma usado nas consultas ao TMDb. Se não informado, utiliza o idioma padrão do sistema.",
                    example = "pt-BR"
            )
            @RequestParam(name = "language", required = false) String language) {
        if (language == null) {
            language = tmdbProperties.getLanguage();
        }

        permissionService.checkCanAddMovieFor(movieSearchRequest.getChooser().getDiscordId());

        ClientResultsResponse response = tmdbService.search(movieSearchRequest.getTitle(), movieSearchRequest.getYear(), language);
        List<TmdbMovieResponse> tmdbMovieResponses = tmdbConverter.toMovieResponseList(response.getResults());

        if (tmdbMovieResponses.size() > 1)
            throw new MultipleMoviesFoundException(tmdbMovieResponses);

        ClientMovieDetailsResponse movieDetails = tmdbService.getMovieDetails(tmdbMovieResponses.getFirst().getId(), language);
        Movie movie = tmdbConverter.toEntityFromClientMovieDetail(movieDetails);

        Long voteId = getVoteId(movieSearchRequest.getVote());

        Movie newMovie = movieService.save(movie, movieSearchRequest.getChooser().getDiscordId(), voteId);
        MovieVoteDetailResponse movieVote = movieConverter.toMovieVoteDetailResponse(newMovie, newMovie.getChooser().getDiscordId());

        return ResponseEntity.status(HttpStatus.CREATED).body(movieVote);
    }

    private static Long getVoteId(Optional<VoteTypeId> voteType) {
        return voteType.map(VoteTypeId::getId).orElse(null);
    }

    @GetMapping
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Listar todos os filmes cadastrados",
            description = "Retorna uma lista paginada de filmes cadastrados, com suporte a filtros, ordenação e paginação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de filmes retornada com sucesso", content = @Content(schema = @Schema(implementation = MoviePage.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
    })
    public ResponseEntity<MoviePage> getAll(
            @ParameterObject
            MovieQueryFilter queryFilter,

            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false) @PositiveOrZero Integer page,

            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false) @Positive Integer size,

            @Parameter(
                    description = "Campo principal usado para ordenação. A ordenação sempre aplica um segundo critério por nome do usuário.",
                    example = "dateAdded"
            )
            @RequestParam(name = "sortBy", defaultValue = "dateAdded", required = false) String sortBy,

            @Parameter(description = "Direção da ordenação (asc ou desc)", example = "desc")
            @RequestParam(name = "sortDir", defaultValue = "desc", required = false) String sortDir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        sort = sort.and(Sort.by(Sort.Direction.ASC, "chooser.name"));

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Movie> movies = movieService.getAll(queryFilter.toSpecification(), pageable);
        MoviePage moviePage = movieConverter.toWithChooserResponseList(movies);

        return ResponseEntity.ok().body(moviePage);
    }

    @GetMapping("/{movieId}")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Buscar filme por ID",
            description = "Retorna os detalhes completos de um filme cadastrado no sistema, incluindo avaliador e informações adicionais."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filme encontrado", content = @Content(schema = @Schema(implementation = MovieWithChooserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
    })
    public ResponseEntity<MovieWithChooserResponse> get(
            @Parameter(
                    description = "Identificador único do filme",
                    example = "42"
            )
            @PathVariable("movieId") Long movieId) {
        Movie movie = movieService.get(movieId);
        return ResponseEntity.ok().body(movieConverter.toWithChooserResponse(movie));
    }

    @DeleteMapping("/{movieId}")
    @CheckOwner(service = MovieService.class)
    @Operation(
            summary = "Excluir filme",
            description = "Remove definitivamente um filme do sistema com base no ID informado."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Filme excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para excluir filmes de outros usuários", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(
            @Parameter(
                    description = "Identificador único do filme",
                    example = "42"
            )
            @PathVariable @ResourceKey Long movieId) {
        movieService.delete(movieId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{discordId}")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Listar filmes por usuário",
            description = "Retorna todos os filmes cadastrados por um usuário específico, identificado pelo seu Discord ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filmes retornados com sucesso", content = @Content(schema = @Schema(implementation = UserWithMoviesResponse.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
    })
    public ResponseEntity<UserWithMoviesResponse> getMoviesByUser(
            @Parameter(
                    description = "Identificador único do usuário",
                    example = "987654321098765432"
            )
            @PathVariable String discordId) {
        User user = userService.getWithMovies(discordId);
        UserWithMoviesResponse response = userConverter.toWithMoviesResponse(user);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/awaiting-review")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Listar filmes aguardando avaliação",
            description = "Retorna uma lista paginada de filmes que ainda não receberam avaliação."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filmes retornados com sucesso", content = @Content(schema = @Schema(implementation = MoviePage.class))),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado", content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
    })
    public ResponseEntity<MoviePage> getMoviesAwaitingReview(
            @ParameterObject
            MovieQueryFilter queryFilter,

            @Parameter(description = "Número da página (inicia em 0)", example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false) @PositiveOrZero Integer page,

            @Parameter(description = "Quantidade de registros por página", example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false) @Positive Integer size,

            @Parameter(description = "Campo usado para ordenação", example = "dateAdded")
            @RequestParam(name = "sortBy", defaultValue = "dateAdded", required = false) String sortBy,

            @Parameter(description = "Direção da ordenação (asc ou desc)", example = "desc")
            @RequestParam(name = "sortDir", defaultValue = "desc", required = false) String sortDir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Movie> movies = movieService.getAll(queryFilter.toSpecification(), pageable);
        MoviePage moviePage = movieConverter.toWithChooserResponseList(movies);

        return ResponseEntity.ok().body(moviePage);
    }

}
