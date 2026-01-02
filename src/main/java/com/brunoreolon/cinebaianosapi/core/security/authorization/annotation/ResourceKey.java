package com.brunoreolon.cinebaianosapi.core.security.authorization.annotation;

import com.brunoreolon.cinebaianosapi.core.security.authorization.SecurityAspect;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marca um parâmetro de método como parte do identificador do recurso.
 *
 * <p>Utilizada pelo {@link SecurityAspect}
 * para extrair dinamicamente valores que compõem o ID de um recurso,
 * especialmente em casos de IDs compostos.</p>
 *
 * <p>O nome do parâmetro deve corresponder ao nome da propriedade
 * no identificador embutido (Embeddable).</p>
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface ResourceKey {
}
