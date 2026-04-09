package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.GroupMemberBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupMemberBanRepository extends JpaRepository<GroupMemberBan, Long> {

    @Query("""
            select (count(b) > 0)
            from GroupMemberBan b
            where b.group.id = :groupId
              and b.member.id = :memberId
              and (b.expiresAt is null or b.expiresAt > :now)
            """)
    boolean existsActiveBan(@Param("groupId") Long groupId,
                            @Param("memberId") Long memberId,
                            @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
            update GroupMemberBan b
            set b.expiresAt = :now
            where b.group.id = :groupId
              and b.member.id = :memberId
              and (b.expiresAt is null or b.expiresAt > :now)
            """)
    int expireActiveBans(@Param("groupId") Long groupId,
                         @Param("memberId") Long memberId,
                         @Param("now") LocalDateTime now);

    @Query("""
            select b
            from GroupMemberBan b
            where b.group.id = :groupId
              and (b.expiresAt is null or b.expiresAt > :now)
            order by b.createdAt desc
            """)
    List<GroupMemberBan> findActiveBansByGroup(@Param("groupId") Long groupId,
                                               @Param("now") LocalDateTime now);

    @Modifying
    @Query("""
            delete from GroupMemberBan b
            where b.expiresAt is not null
              and b.expiresAt <= :now
            """)
    int deleteExpiredBans(@Param("now") LocalDateTime now);

}