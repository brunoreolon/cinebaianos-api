package com.brunoreolon.cinebaianosapi.core.security;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

public @interface CheckSecurity {

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface CanAccess { }

    @PreAuthorize("hasRole('ADMIN')")
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface IsAdmin { }

    @PreAuthorize("hasRole('ADMIN') or @authz.isBot()")
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface IsAdminOrBot { }

    @PreAuthorize("@authz.isBot()")
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface IsBot { }

    @Retention(RUNTIME)
    @Target(METHOD)
    @interface IsOwner {
        String service();
    }

    @Retention(RUNTIME)
    @Target(METHOD)
    @interface IsOwnerOrAdmin {
        String service();
    }

    @Retention(RUNTIME)
    @Target(METHOD)
    @interface IsOwnerVote {
        String service();
    }

    @Retention(RUNTIME)
    @Target(METHOD)
    @interface IsOwnerOrBot {
        String service();
    }

    @Retention(RUNTIME)
    @Target(METHOD)
    @interface IsOwnerVoteOrBot {
        String service();
    }

}
