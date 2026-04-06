package com.brunoreolon.cinebaianosapi.core.security.authorization.interfaces;

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
        throw new UnsupportedOperationException("ownable.buildid.composite_not_supported");
    }

    /**
     * Retorna o nome do campo no ID composto que representa o usuário autenticado.
     *
     * <p>Quando um endpoint possui apenas uma {@code @ResourceKey} visível na URL
     * mas o recurso requer um ID composto, o Aspect de segurança usa este nome
     * para complementar automaticamente o mapa de chaves com o userId do contexto.</p>
     *
     * <p>Exemplo: {@code GroupMemberService} retorna {@code "memberId"},
     * {@code VoteService} retorna {@code "voterId"}.</p>
     *
     * <p>Retorne {@code null} (padrão) se o serviço não precisa deste comportamento.</p>
     *
     * @return nome do campo do userId no ID composto, ou {@code null}
     */
    default String currentUserKeyName() {
        return null;
    }

}