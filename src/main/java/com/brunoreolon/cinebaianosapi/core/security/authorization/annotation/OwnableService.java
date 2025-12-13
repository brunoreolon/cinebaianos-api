package com.brunoreolon.cinebaianosapi.core.security.authorization.annotation;

import com.brunoreolon.cinebaianosapi.core.security.authorization.ResourceKeyValues;

/**
 * Porta de acesso a entidades {@link Ownable}, utilizada pelo mecanismo
 * de autorização para verificar ownership.
 *
 * <p>Essa interface abstrai a forma de recuperar uma entidade e construir
 * seu identificador, permitindo que a camada de segurança não dependa
 * de implementações concretas.</p>
 *
 * <p>Verificar se um recurso pertence a um usuário.</p>
 *
 * @param <T>  tipo da entidade ownable
 * @param <ID> tipo do identificador da entidade
 */
public interface OwnableService<T extends Ownable, ID> {

    /**
     * Recupera a entidade a partir do seu identificador.
     *
     * @param id identificador do recurso
     * @return entidade encontrada
     */
    T get(ID id);

    /**
     * Constrói um identificador (simples ou composto) a partir
     * de valores extraídos do endpoint.
     *
     * <p>Implementações que não utilizam IDs compostos podem
     * ignorar este método.</p>
     *
     * @param keyValues valores do identificador
     * @return identificador construído
     */
    default ID buildId(ResourceKeyValues keyValues) {
        throw new UnsupportedOperationException("Composite ID not supported here");
    }

}
