package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("""
            select distinct g
            from Group g
            left join fetch g.owner
            left join fetch g.movies gm
            left join fetch gm.chooser
            left join fetch gm.movie m
            left join fetch m.genres
            left join fetch gm.votes v
            left join fetch v.voter
            left join fetch v.vote
            where g.id = :groupId
            order by g.id
            """)
    Optional<Group> findGroupWithMovies(Long groupId);

    boolean existsByTagIgnoreCase(String tag);

    boolean existsByTagIgnoreCaseAndIdNot(String tag, Long id);

    Optional<Group> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);

    @Query("""
            select g
            from Group g
            where g.active = true
            and g.visibility = 'PUBLIC'
            order by g.id
            """)
    List<Group> findAllPublicGroups();

    // Ponto para melhorar
    @Query("""
            select distinct g
            from Group g
            left join fetch g.members m
            where g.id = :groupId
            and g.active = true
            order by g.id
            """)
    Group findGroupWithMembers(@Param("groupId") Long groupId);

    @Query("""
            select g 
            from Group g
            where g.visibility = 'PUBLIC' 
            and g.active = true
            and not exists (
                select 1 from GroupMember m 
                where m.group.id = g.id 
                and m.member.id = :userId 
                and m.active = true
            )
            """)
    List<Group> findPublicGroupsNotJoinedByUser(Long userId);

    @Query("""
            select g from Group g 
            where g.owner.id = :ownerId 
            and g.active = true
            """)
    List<Group> findByOwnerId(Long ownerId);

    @Query("""
            select g from Group g
            join g.members m
            where m.member.id = :userId 
            and m.active = true
            """)
    List<Group> findGroupsByMemberId(Long userId);

    @Query("""
            select g
            from Group g
            left join fetch g.owner
            order by g.createdAt desc
            """)
    List<Group> findAllForAdmin();

//    @Query("""
//            from Group g
//            left join fetch VoteType vt
//            order by vt.id
//            """)
//    Optional<Group> findAllByVoteTypes(Long groupId);

    @Modifying
    @Query("""
            update Group g
            set g.bannedAt = null,
                g.bannedBy = null,
                g.banReason = null,
                g.expiresAt = null
            where g.bannedAt is not null
              and g.expiresAt is not null
              and g.expiresAt <= :now
            """)
    int clearExpiredBans(@Param("now") LocalDateTime now);

}