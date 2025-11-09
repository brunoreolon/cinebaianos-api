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

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.function.Predicate;

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
        checkOwnerVote(joinPoint, isOwnerVote.service(), false);
    }

    @Before("@annotation(IsOwnerVoteOrBot)")
    public void checkOwnershipVoteOrBot(JoinPoint joinPoint, CheckSecurity.IsOwnerVoteOrBot isOwnerVoteOrBot) {
        checkOwnerVote(joinPoint, isOwnerVoteOrBot.service(), false);
    }

    @Before("@annotation(isOwnerOrAdmin)")
    public void checkOwnerOrAdmin(JoinPoint joinPoint, CheckSecurity.IsOwnerOrAdmin isOwnerOrAdmin) {
        OwnableService<?, ?> service = (OwnableService<?, ?>) context.getBean(isOwnerOrAdmin.service());
        Object resourceId = getResourceId(joinPoint);

        boolean autorizado = applicationService.isOwnerOrAdmin(service, resourceId);

        if (!autorizado) {
            throw new AccessDeniedException("You are not allowed to modify this resource.");
        }
    }

    private void checkOwnerVote(JoinPoint joinPoint, String serviceBeanName, boolean allowBot) {
        OwnableService<?, ?> service = (OwnableService<?, ?>) context.getBean(serviceBeanName);

        String discordId = (String) getAnnotatedParam(joinPoint, "discordId");
        Long movieId = (Long) getAnnotatedParam(joinPoint, "movieId");

        if (discordId == null || movieId == null) {
            throw new IllegalStateException("discordId or movieId not found in method parameters");
        }

        VoteKey key = new VoteKey(discordId, movieId);

        boolean authorized = allowBot
                ? applicationService.isOwnerOrBot(service, key)
                : applicationService.isOwner(service, key);

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
        return findParameterByAnnotation(joinPoint, ResourceId.class, ann -> true);
    }

    private static Object getAnnotatedParam(JoinPoint joinPoint, String name) {
        return findParameterByAnnotation(joinPoint, ResourceId.class, ann -> ((ResourceId) ann).name().equals(name));
    }

    private static Object findParameterByAnnotation(JoinPoint joinPoint, Class<? extends Annotation> annotationType,
                                                    Predicate<Annotation> filter) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Annotation annotation = parameters[i].getAnnotation(annotationType);
            if (annotation != null && filter.test(annotation)) {
                return args[i];
            }
        }
        return null;
    }

}