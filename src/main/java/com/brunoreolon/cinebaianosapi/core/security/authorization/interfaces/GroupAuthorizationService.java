package com.brunoreolon.cinebaianosapi.core.security.authorization.interfaces;

import com.brunoreolon.cinebaianosapi.core.security.authorization.enums.GroupMemberRole;

/**
 * Interface para serviços que fornecem verificações de autorização relacionadas a grupos.
 * Usada pelo mecanismo de segurança para verificar membership, roles e permissões de gerenciamento em grupos.
 */
public interface GroupAuthorizationService {

    /**
     * Verifica se o grupo informado existe.
     *
     * @param groupId ID do grupo
     * @return true se o grupo existe
     */
    boolean groupExists(Long groupId);

    /**
     * Verifica se o usuário é membro ativo do grupo.
     *
     * @param groupId ID do grupo
     * @param userId ID do usuário
     * @return true se o usuário é membro ativo do grupo
     */
    boolean isMember(Long groupId, Long userId);

    /**
     * Verifica se o usuário tem a função especificada no grupo.
     *
     * @param groupId ID do grupo
     * @param userId ID do usuário
     * @param requiredRole função requerida
     * @return true se o usuário tem a função no grupo
     */
    boolean hasRole(Long groupId, Long userId, GroupMemberRole requiredRole);

}