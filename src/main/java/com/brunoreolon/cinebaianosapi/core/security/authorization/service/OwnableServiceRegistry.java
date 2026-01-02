package com.brunoreolon.cinebaianosapi.core.security.authorization.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.OwnableService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OwnableServiceRegistry {

    private final Map<Class<?>, OwnableService<?, ?>> servicesByType = new HashMap<>();

    public OwnableServiceRegistry(List<OwnableService<?, ?>> services) {
        for (OwnableService<?, ?> service : services) {
            servicesByType.put(service.getClass(), service);
        }
    }

    public OwnableService<?, ?> get(Class<?> type) {
        OwnableService<?, ?> found = servicesByType.get(type);
        if (found != null) return found;

        return servicesByType.entrySet().stream()
                .filter(e -> type.isAssignableFrom(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
//                .orElseThrow(() -> new IllegalStateException(
//                        "OwnableService não encontrado para " + type.getName()
//                ));
                .orElseThrow(() -> new IllegalStateException("ownable.service.not.found"));
    }

}