package com.brunoreolon.cinebaianosapi.core.security.authorization;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.CheckSecurity;
import com.brunoreolon.cinebaianosapi.core.security.authorization.exception.OwnershipAccessDeniedException;
import com.brunoreolon.cinebaianosapi.core.security.authorization.service.AuthorizationService;
import com.brunoreolon.cinebaianosapi.core.security.authorization.service.OwnableServiceRegistry;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.OwnableService;
import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.ResourceKey;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Aspect que intercepta endpoints anotados com @RequireRole ou @CheckOwner.
 *
 * - @RequireRole: verifica se o usuário possui a role ou se é bot do discord autorizado.
 * - @CheckOwner: verifica se o usuário é dono do recurso ou se é bot/admin autorizado.
 */
@Aspect
@Component
@AllArgsConstructor
public class SecurityAspect {

    private final AuthorizationService authorizationService;
    private final OwnableServiceRegistry serviceRegistry;

    /** Intercepta RequireRole e lança exceção se o usuário não tiver permissão */
    @Before("@annotation(requireRole)")
    public void checkRole(JoinPoint joinPoint, CheckSecurity.RequireRole requireRole) {
        if (!authorizationService.hasAnyRole(requireRole.roles()) && !(requireRole.allowBot() && authorizationService.isBot())) {
            throw new OwnershipAccessDeniedException("User does not have required role");
        }
    }

    /** Intercepta CheckOwner e lança exceção se o usuário não for dono do recurso */
    @Before("@annotation(checkOwner)")
    public void checkOwnership(JoinPoint joinPoint, CheckSecurity.CheckOwner checkOwner) {
        OwnableService<?, ?> service = serviceRegistry.get(checkOwner.service());
        Object id = extractId(joinPoint, service);

        boolean authorized = authorize(service, id, checkOwner);

        if (!authorized) {
            throw new OwnershipAccessDeniedException("You are not allowed to modify this resource");
        }
    }

    /** Recupera os valores dos parâmetros anotados com @ResourceKey */
    private Object extractId(JoinPoint joinPoint, OwnableService<?, ?> service) {
        Object[] args = joinPoint.getArgs();
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        Parameter[] params = sig.getMethod().getParameters();

        Map<String, Object> found = new HashMap<>();
        for (int i = 0; i < params.length; i++) {
            if (params[i].isAnnotationPresent(ResourceKey.class)) {
                found.put(params[i].getName(), args[i]);
            }
        }

        if (found.size() == 1) {
            return found.values().iterator().next();
        }

//        return service.buildId(found);
        return service.buildId(new ResourceKeyValues(found));
    }

    /** Lógica de autorização final: ownership, admin, bot */
    private boolean authorize(OwnableService<?, ?> service, Object id, CheckSecurity.CheckOwner check) {
        if (check.allowBot() && authorizationService.isBot()) return true;
        if (check.allowAdmin() && authorizationService.isAdmin()) return true;

        return authorizationService.isOwner(service, id);
    }

}