package com.brunoreolon.cinebaianosapi.domain.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import com.brunoreolon.cinebaianosapi.domain.exception.GroupMovieAlreadyExistsException;
import com.brunoreolon.cinebaianosapi.domain.exception.GroupMovieNotFoundException;
import com.brunoreolon.cinebaianosapi.domain.exception.GroupMovieInvalidOperationException;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMovie;
import com.brunoreolon.cinebaianosapi.domain.model.Group;
import com.brunoreolon.cinebaianosapi.domain.model.Movie;
import com.brunoreolon.cinebaianosapi.domain.model.User;
import com.brunoreolon.cinebaianosapi.domain.repository.GroupMovieRepository;
import com.brunoreolon.cinebaianosapi.domain.repository.MovieRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class GroupMovieService {

    private final GroupMovieRepository groupMovieRepository;
    private final MovieRepository movieRepository;
    private final GroupService groupService;
    private final MovieService movieService;
    private final UserRegistratioService userService;
    private final GroupMemberService groupMemberService;

    /**
     * Adiciona um filme a um grupo a partir de uma Movie existente ou cria a Movie se não existir.
     * Validações:
     * - Se onlyAdminAddMovie = true, apenas admins podem adicionar
     * - Um filme não pode ser adicionado mais de uma vez ao mesmo grupo
     */
    @Transactional
    public GroupMovie addMovieToGroup(Long groupId, Movie movie, Long chooserId) {
        Group group = groupService.getById(groupId);
        User user = userService.get(chooserId);

        if (group.getOnlyAdminAddMovie()) {
            boolean isAdmin = groupMemberService.hasRole(groupId, chooserId, GroupMemberRole.ADMIN);
            if (!isAdmin) {
                throw new GroupMovieInvalidOperationException("only.admin.can.add.movies.message");
            }
        }

        // Salva o filme globalmente se ainda não existir ou reutiliza o existente
        Optional<Movie> existingMovie = movieRepository.findByTmdbId(movie.getTmdbId());
        Movie savedMovie = existingMovie.orElseGet(() -> movieService.save(movie, chooserId, null));

        // Verifica se o filme já foi adicionado a este grupo
        if (groupMovieRepository.existsByGroupIdAndMovieId(groupId, savedMovie.getId())) {
            throw new GroupMovieAlreadyExistsException(
                    "group.movie.already.exists.message",
                    new Object[]{groupId, savedMovie.getTitle()}
            );
        }

        GroupMovie groupMovie = GroupMovie.builder()
                .group(group)
                .movie(savedMovie)
                .chooser(user)
                .build();

        return groupMovieRepository.save(groupMovie);
    }

    /**
     * Remove um filme de um grupo.
     * Apenas o usuário que adicionou o filme ou um admin do grupo podem remover.
     */
    @Transactional
    public void removeMovieFromGroup(Long groupId, Long movieId, Long userId) {
        GroupMovie groupMovie = groupMovieRepository.findByGroupIdAndMovieId(groupId, movieId)
                .orElseThrow(() -> new GroupMovieNotFoundException(
                        "group.movie.not.found.message",
                        new Object[]{groupId, movieId}
                ));

        // Verifica se o usuário é o que adicionou o filme
        if (!groupMovie.getChooser().getId().equals(userId)) {
            // Se não é o que adicionou, verifica se é admin do grupo
                    boolean isAdmin = groupMemberService.hasRole(groupId, userId, GroupMemberRole.ADMIN);
            if (!isAdmin) {
                throw new GroupMovieInvalidOperationException("only.chooser.or.admin.can.remove.movie");
            }
        }

        groupMovieRepository.delete(groupMovie);
    }

    /**
     * Obtém um filme específico de um grupo
     */
    public GroupMovie getGroupMovie(Long groupId, Long movieId) {
        return groupMovieRepository.findByGroupIdAndMovieId(groupId, movieId)
                .orElseThrow(() -> new GroupMovieNotFoundException(
                        "group.movie.not.found.message",
                        new Object[]{groupId, movieId}
                ));
    }

    /**
     * Lista todos os filmes de um grupo
     */
    public List<GroupMovie> getGroupMovies(Long groupId) {
        // Verifica se o grupo existe
        groupService.getById(groupId);
        return groupMovieRepository.findByGroupId(groupId);
    }

    /**
     * Remove todos os filmes de um grupo (usado quando o grupo é deletado)
     */
    @Transactional
    public void removeAllMoviesFromGroup(Long groupId) {
        groupMovieRepository.deleteByGroupId(groupId);
    }

}