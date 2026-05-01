package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.GroupMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMovieRepository extends JpaRepository<GroupMovie, Long> {

    /**
     * Encontra um filme em um grupo específico
     */
    Optional<GroupMovie> findByGroupIdAndMovieId(Long groupId, Long movieId);

    /**
     * Lista todos os filmes de um grupo
     */
    List<GroupMovie> findByGroupId(Long groupId);

    /**
     * Verifica se um filme já foi adicionado a um grupo
     */
    boolean existsByGroupIdAndMovieId(Long groupId, Long movieId);

    /**
     * Remove todos os filmes de um grupo (caso o grupo seja deletado)
     */
    void deleteByGroupId(Long groupId);
}