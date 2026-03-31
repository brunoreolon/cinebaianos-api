package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("""
            select distinct g
            from Group g
            left join fetch g.movies gm
            left join fetch gm.movie m
            left join fetch m.genres
            where g.id = :groupId
            order by g.id
            """)
    Optional<Group> findGroupWithMovies(Long groupId);

    Optional<Group> findByTag(String tag);

    Optional<Group> findBySlug(String slug);

//    @Query("""
//            from Group g
//            left join fetch VoteType vt
//            order by vt.id
//            """)
//    Optional<Group> findAllByVoteTypes(Long groupId);

}