package com.brunoreolon.cinebaianosapi.core.security.authorization.annotation;

/**
 * Marca uma entidade como pertencente a um dono (ownership).
 *
 * <p>Essa interface faz parte do domínio e representa uma regra de negócio:
 * determinadas entidades só podem ser manipuladas pelo seu proprietário.</p>
 *
 * <p>O valor retornado por {@link #getOwnerId()} será comparado com o usuário
 * autenticado no contexto de segurança.</p>
 *
 * @param <ID> tipo do identificador do dono (ex: String, Long)
 */
public interface Ownable<ID> {

    /**
     * Retorna o identificador do dono da entidade.
     *
     * @return identificador do proprietário
     */
    ID getOwnerId();

}
