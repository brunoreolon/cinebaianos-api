package com.brunoreolon.cinebaianosapi.core.security;

import com.brunoreolon.cinebaianosapi.api.model.vote.id.VoteKey;
import com.brunoreolon.cinebaianosapi.domain.model.OwnableService;
import com.brunoreolon.cinebaianosapi.domain.model.ResourceId;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

@Aspect
@Component
@AllArgsConstructor
public class SecurityAspect {

    private final ApplicationService applicationService;
    private final ApplicationContext context;

    @Before("@annotation(isOwner)")
    public void checkOwnership(JoinPoint joinPoint, CheckSecurity.IsOwner isOwner) {
        checkOwner(joinPoint, isOwner.service(), false);
    }

    @Before("@annotation(isOwnerOrBot)")
    public void checkOwnershipOrBot(JoinPoint joinPoint, CheckSecurity.IsOwnerOrBot isOwnerOrBot) {
        checkOwner(joinPoint, isOwnerOrBot.service(), true);
    }

    @Before("@annotation(isOwnerVote)")
    public void checkOwnershipVote(JoinPoint joinPoint, CheckSecurity.IsOwnerVote isOwnerVote) {
        OwnableService<?, ?> service = (OwnableService<?, ?>) context.getBean(isOwnerVote.service());

        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();

        String discordId = null;
        Long movieId = null;

        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(ResourceId.class)) {
                if ("discordId".equals(parameters[i].getAnnotation(ResourceId.class).name())) {
                    discordId = (String) args[i];
                } else if ("movieId".equals(parameters[i].getAnnotation(ResourceId.class).name())) {
                    movieId = (Long) args[i];
                }
            }
        }

        if (discordId == null || movieId == null) {
            throw new IllegalStateException("discordId or movieId not found in method parameters");
        }

        boolean authorized = applicationService.isOwner(service, new VoteKey(discordId, movieId));

        if (!authorized) {
            throw new AccessDeniedException("You are not allowed to modify this vote");
        }
    }

    private void checkOwner(JoinPoint joinPoint, String serviceBeanName, boolean allowBot) {
        OwnableService<?, ?> service = (OwnableService<?, ?>) context.getBean(serviceBeanName);

        Object resourceId = getResourceId(joinPoint);

        if (resourceId == null) {
            throw new IllegalStateException("No parameter annotated with @ResourceId found");
        }

        boolean authorized = allowBot
                ? applicationService.isOwnerOrBot(service, resourceId)
                : applicationService.isOwner(service, resourceId);

        if (!authorized) {
            throw new AccessDeniedException("You are not allowed to modify this resource");
        }
    }

    private static Object getResourceId(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();

        Object resourceId = null;
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(ResourceId.class)) {
                resourceId = args[i];
                break;
            }
        }

        return resourceId;
    }

}