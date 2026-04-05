package com.brunoreolon.cinebaianosapi.core.security.authorization.service;

import com.brunoreolon.cinebaianosapi.core.security.authorization.annotation.GroupAuthorizationService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GroupAuthorizationServiceRegistry {

    private final Map<Class<?>, GroupAuthorizationService> servicesByType = new HashMap<>();

    public GroupAuthorizationServiceRegistry(List<GroupAuthorizationService> services) {
        for (GroupAuthorizationService service : services) {
            servicesByType.put(service.getClass(), service);
        }
    }

    public GroupAuthorizationService get(Class<?> type) {
        GroupAuthorizationService found = servicesByType.get(type);
        if (found != null) return found;

        return servicesByType.entrySet().stream()
                .filter(e -> type.isAssignableFrom(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("group.authorization.service.not.found"));
    }

}