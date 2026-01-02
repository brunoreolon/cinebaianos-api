package com.brunoreolon.cinebaianosapi.core.security.authorization;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Utilitário responsável por construir identificadores
 * simples ou compostos a partir de valores extraídos
 * de métodos de controller.
 *
 * <p>Utiliza JavaBeans Introspector para popular
 * propriedades via setters, evitando dependência
 * de construtores, ordem de parâmetros ou flags
 * de compilação.</p>
 *
 * <p>Usado principalmente pelo mecanismo de autorização
 * para reconstruir IDs de recursos protegidos.</p>
 */
public class ResourceKeyValues {

    private final Map<String, Object> values;

    public ResourceKeyValues(Map<String, Object> values) {
        this.values = values;
    }

    public <T> T as (Class<T> type) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();

            var info = Introspector.getBeanInfo(type, Object.class);

            for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
                String property = pd.getName();
                Object value = values.get(property);

                if (value != null && pd.getWriteMethod() != null) {
                    pd.getWriteMethod().invoke(instance, value);
                }
            }

            return instance;
        } catch (InstantiationException |
                 IllegalAccessException |
                 InvocationTargetException |
                 NoSuchMethodException |
                 java.beans.IntrospectionException e) {
//            throw new IllegalStateException("Error constructing ID of type " + type.getSimpleName(), e);
            throw new IllegalStateException("id.constructor.error");
        }
    }

}