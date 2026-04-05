package com.brunoreolon.cinebaianosapi.core.security.authorization.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.brunoreolon.cinebaianosapi.domain.model.GroupMemberRole;
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

    /**
     * Verifica se o usuário logado é membro do grupo especificado.
     * Pode permitir administradores e bots do discord opcionalmente.
     *
     * Exemplo de uso:
     * @CheckGroupMember(service = GroupMemberService.class, allowAdmin = true)
     */
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface CheckGroupMember {
        Class<? extends GroupAuthorizationService> service(); // Serviço que fornece verificações de grupo
        boolean allowAdmin() default false; // Permitir admin
        boolean allowBot() default false;   // Permitir bot do discord
    }

    /**
     * Verifica se o usuário logado tem a função especificada no grupo.
     * Pode permitir administradores e bots do discord opcionalmente.
     *
     * Exemplo de uso:
     * @CheckGroupRole(service = GroupMemberService.class, role = GroupMemberRole.OWNER, allowAdmin = true)
     */
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface CheckGroupRole {
        Class<? extends GroupAuthorizationService> service(); // Serviço que fornece verificações de grupo
        GroupMemberRole role();             // Função requerida no grupo
        boolean allowAdmin() default false; // Permitir admin
        boolean allowBot() default false;   // Permitir bot do discord
    }

}