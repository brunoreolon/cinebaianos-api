package com.brunoreolon.cinebaianosapi.core.security.authorization;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.*;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupMember;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckGroupRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.CheckOwner;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity.RequireRole;
import com.brunoreolon.cinebaianosapi.core.security.authorization.exception.AuthorizationGroupNotFoundException;
import com.brunoreolon.cinebaianosapi.core.security.authorization.exception.OwnershipAccessDeniedException;
import com.brunoreolon.cinebaianosapi.core.security.authorization.service.AuthorizationService;
import com.brunoreolon.cinebaianosapi.core.security.authorization.service.OwnableServiceRegistry;
import com.brunoreolon.cinebaianosapi.core.security.authorization.service.GroupAuthorizationServiceRegistry;
import com.brunoreolon.cinebaianosapi.domain.model.GroupMemberRole;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect que intercepta endpoints anotados com @RequireRole, @CheckOwner, @CheckGroupMember, @CheckGroupRole ou @CheckGroupManager.
 * <p>
 * - @RequireRole: verifica se o usuário possui a role ou se é bot do discord autorizado.
 * - @CheckOwner: verifica se o usuário é dono do recurso ou se é bot/admin autorizado.
 * - @CheckGroupMember: verifica se o usuário é membro do grupo ou se é bot/admin autorizado.
 * - @CheckGroupRole: verifica se o usuário tem a função especificada no grupo ou se é bot/admin autorizado.
 */
@AllArgsConstructor
@Aspect
@Component
public class SecurityAspect {

    private final AuthorizationService authorizationService;
    private final OwnableServiceRegistry serviceRegistry;
    private final GroupAuthorizationServiceRegistry groupAuthorizationServiceRegistry;

    /**
     * Intercepta RequireRole e lança exceção se o usuário não tiver permissão
     */
    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, RequireRole requireRole) {
        if (!authorizationService.hasAnyRole(requireRole.roles()) && !(requireRole.allowBot() && authorizationService.isBot())) {
            throw new OwnershipAccessDeniedException("auth.required_role.message");
        }
    }

    /**
     * Intercepta CheckOwner e lança exceção se o usuário não for dono do recurso
     */
    @Before("@annotation(checkOwner)")
    public void checkOwnership(JoinPoint joinPoint, CheckOwner checkOwner) {
        OwnableService<?, ?> service = serviceRegistry.get(checkOwner.service());
        Object resourceId = extractId(joinPoint, ResourceKey.class, service);

        boolean authorized = authorize(service, resourceId, checkOwner);

        if (!authorized) {
            throw new OwnershipAccessDeniedException("auth.resource_not_owned.message");
        }
    }

    /**
     * Intercepta CheckGroupMember e lança exceção se o usuário não for membro do grupo
     */
    @Before("@annotation(checkGroupMember)")
    public void checkGroupMembership(JoinPoint joinPoint, CheckGroupMember checkGroupMember) {
        GroupAuthorizationService service = groupAuthorizationServiceRegistry.get(checkGroupMember.service());
        Long groupId = (Long) extractId(joinPoint, GroupKey.class, null);

        ensureGroupExists(service, groupId);

        boolean authorized = authorizeGroupAccess(service, groupId, null, checkGroupMember.allowAdmin(), checkGroupMember.allowBot());

        if (!authorized) {
            throw new OwnershipAccessDeniedException("auth.not_group_member.message");
        }
    }

    /**
     * Intercepta CheckGroupRole e lança exceção se o usuário não tiver a função no grupo
     */
    @Before("@annotation(checkGroupRole)")
    public void checkGroupRole(JoinPoint joinPoint, CheckGroupRole checkGroupRole) {
        GroupAuthorizationService service = groupAuthorizationServiceRegistry.get(checkGroupRole.service());
        Long groupId = (Long) extractId(joinPoint, GroupKey.class, null);

        ensureGroupExists(service, groupId);

        boolean authorized = authorizeGroupAccess(service, groupId, checkGroupRole.role(), checkGroupRole.allowAdmin(), checkGroupRole.allowBot());

        if (!authorized) {
            throw new OwnershipAccessDeniedException("auth.insufficient_group_role.message");
        }
    }

    private Object extractId(JoinPoint joinPoint, Class<? extends Annotation> annotationClass, OwnableService<?, ?> service) {
        Object[] args = joinPoint.getArgs();
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Parameter[] params = sig.getMethod().getParameters();

        Map<String, Object> found = new HashMap<>();
        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(annotationClass)) {
                found.put(params[i].getName(), args[i]);
            }
        }

        if (found.isEmpty()) return null;
        if (found.size() == 1 && service == null) return found.values().iterator().next();

        // Se há apenas 1 chave mas o serviço é fornecido (esperando chave composta),
        // tenta completá-la com o userId do contexto de autenticação
        if (found.size() == 1 && service != null) {
            String userKeyName = service.currentUserKeyName();
            if (userKeyName != null) {
                Long userId = authorizationService.getCurrentUserId();
                if (userId != null) {
                    found.put(userKeyName, userId);
                    return service.buildId(new ResourceKeyValues(found));
                }
            }
            // Se não há campo de usuário configurado ou userId indisponível, retorna o valor simples
            return found.values().iterator().next();
        }

        if (found.size() > 1 && service != null) {
            return service.buildId(new ResourceKeyValues(found));
        }

        throw new IllegalArgumentException("Múltiplas chaves encontradas, mas nenhum serviço fornecido para construir ID composto");
    }

    /**
     * Lógica de autorização final: ownership, admin, bot
     */
    private boolean authorize(OwnableService<?, ?> service, Object id, CheckOwner check) {
        if (check.allowBot() && authorizationService.isBot()) return true;
        if (check.allowAdmin() && authorizationService.isAdmin()) return true;

        return authorizationService.isOwner(service, id);
    }

    private boolean authorizeGroupAccess(GroupAuthorizationService service, Long groupId, GroupMemberRole requiredRole,
                                         boolean allowAdmin, boolean allowBot) {
        if (allowBot && authorizationService.isBot()) return true;
        if (allowAdmin && authorizationService.isAdmin()) return true;

        return authorizationService.authorize(service, groupId, requiredRole);
    }

    private void ensureGroupExists(GroupAuthorizationService service, Long groupId) {
        if (!service.groupExists(groupId)) {
            throw new AuthorizationGroupNotFoundException(new Object[]{groupId});
        }
    }

}