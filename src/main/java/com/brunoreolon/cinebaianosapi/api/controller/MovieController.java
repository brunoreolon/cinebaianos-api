package com.brunoreolon.cinebaianosapi.api.controller;

import com.brunoreolon.cinebaianosapi.api.converter.MovieConverter;
import com.brunoreolon.cinebaianosapi.api.converter.TmdbConverter;
import com.brunoreolon.cinebaianosapi.api.converter.UserConverter;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
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
            @ApiResponse(responseCode = "201", description = "Filme cadastrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para adicionar filmes para outro usuário"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado no TMDb")
    })
    public ResponseEntity<MovieWithChooserResponse> addById(@Valid @RequestBody MovieIdRequest movieIdRequest,
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
            description = "Realiza uma busca no TMDb utilizando título e ano. "
                    + "Caso apenas um resultado seja encontrado, o filme é cadastrado. "
                    + "Se múltiplos resultados forem encontrados, uma exceção contendo as opções será retornada."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Filme cadastrado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para adicionar filmes para outro usuário"),
            @ApiResponse(responseCode = "404", description = "Nenhum filme correspondente encontrado no TMDb"),
            @ApiResponse(responseCode = "409", description = "Mais de um filme encontrado para os critérios informados")
    })
    public ResponseEntity<MovieVoteDetailResponse> searchAndAddMovie(@Valid @RequestBody MovieSearchRequest movieSearchRequest,
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
            @ApiResponse(responseCode = "200", description = "Lista de filmes retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    public ResponseEntity<MoviePage> getAll(
            MovieQueryFilter queryFilter,
            @RequestParam(name = "page", defaultValue = "0", required = false) @PositiveOrZero Integer page,
            @RequestParam(name = "size", defaultValue = "10", required = false) @Positive Integer size,
            @RequestParam(name = "sortBy", defaultValue = "dateAdded", required = false) String sortBy,
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
            @ApiResponse(responseCode = "200", description = "Filme encontrado"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado")
    })
    public ResponseEntity<MovieWithChooserResponse> get(@PathVariable("movieId") Long movieId) {
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
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não possui permissão para excluir filmes de outros usuários"),
            @ApiResponse(responseCode = "404", description = "Filme não encontrado")
    })
    public ResponseEntity<Void> delete(@PathVariable @ResourceKey Long movieId) {
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
            @ApiResponse(responseCode = "200", description = "Filmes retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<UserWithMoviesResponse> getMoviesByUser(@PathVariable String discordId) {
        User user = userService.getWithMovies(discordId);
        UserWithMoviesResponse response = userConverter.toWithMoviesResponse(user);

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/awaiting-review")
    @RequireRole(roles = {Role.ADMIN, Role.USER})
    @Operation(
            summary = "Listar filmes por usuário",
            description = "Retorna todos os filmes cadastrados por um usuário específico, identificado pelo seu Discord ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Filmes retornados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    public ResponseEntity<MoviePage> getMoviesAwaitingReview(
            MovieQueryFilter queryFilter,
            @RequestParam(name = "page", defaultValue = "0", required = false) @PositiveOrZero Integer page,
            @RequestParam(name = "size", defaultValue = "10", required = false) @Positive Integer size,
            @RequestParam(name = "sortBy", defaultValue = "dateAdded", required = false) String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "desc", required = false) String sortDir
    ) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Movie> movies = movieService.getAll(queryFilter.toSpecification(), pageable);
        MoviePage moviePage = movieConverter.toWithChooserResponseList(movies);

        return ResponseEntity.ok().body(moviePage);
    }

}
