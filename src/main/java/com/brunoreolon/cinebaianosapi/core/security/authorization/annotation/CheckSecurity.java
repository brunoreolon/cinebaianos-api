package com.brunoreolon.cinebaianosapi.core.security.authorization.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.brunoreolon.cinebaianosapi.domain.model.Role;

/**
 * Annotations para controle de segurança nos endpoints.
 */
public @interface CheckSecurity {

    /**
     * Verifica se o usuário possui qualquer uma das roles especificadas.
     * Também pode permitir bots do discord se allowBot = true.
     *
     * Exemplo de uso:
     * @RequireRole(roles = {Role.ADMIN, Role.USER}, allowBot = true)
     */
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface RequireRole {
        Role[] roles();                   // Roles permitidas
        boolean allowBot() default false; // Permitir execução por bot do discord
    }

    /**
     * Verifica se o usuário logado é dono do recurso.
     * Pode permitir administradores e bots do discord opcionalmente.
     *
     * Exemplo de uso:
     * @CheckOwner(service = MovieService.class, allowAdmin = true)
     */
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface CheckOwner {
        Class<? extends OwnableService<?, ?>> service(); // Serviço que fornece o recurso
        boolean allowAdmin() default false;              // Permitir admin
        boolean allowBot() default false;                // Permitir bot do discord
    }

}
