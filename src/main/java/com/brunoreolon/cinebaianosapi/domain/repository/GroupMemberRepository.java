package com.brunoreolon.cinebaianosapi.domain.repository;

import com.brunoreolon.cinebaianosapi.domain.model.GroupMember;
import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupIdAndMemberIdAndActiveTrue(Long groupId, Long memberId);

    boolean existsByGroupIdAndMemberIdAndActiveTrueAndRoleIn(Long groupId, Long memberId, Collection<GroupMemberRole> roles);

    Optional<GroupMember> findByGroupIdAndMemberId(Long groupId, Long userId);

    List<GroupMember> findByGroupIdAndActiveTrue(Long groupId);

    List<GroupMember> findByMemberIdAndActiveTrue(Long groupId);

    List<GroupMember> findByMemberIdAndActiveTrueOrderByJoinedAtDesc(Long memberId);

    Optional<GroupMember> findByMemberIdAndActiveTrueAndSelectedTrue(Long memberId);

    // Contar membros ativos
    Long countByGroupIdAndActiveTrue(Long groupId);

    // Verificar se usuário é admin/owner
    boolean existsByGroupIdAndMemberIdAndRoleIn(Long groupId, Long memberId, Collection<GroupMemberRole> roles);

    // Encontrar todos os admins de um grupo
    List<GroupMember> findByGroupIdAndRoleAndActiveTrue(Long groupId, GroupMemberRole role);

}