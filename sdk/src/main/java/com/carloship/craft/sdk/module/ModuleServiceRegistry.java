package com.carloship.craft.sdk.module;

import java.util.HashMap;
import java.util.Map;

public class ModuleServiceRegistry {

    private final Map<Class<?>, Object> services = new HashMap<>();

    public <T> void register(Class<T> type, T instance) {
        services.put(type, instance);
    }

    public boolean containsAssignable(Class<?> type) {

        return services.keySet().stream().anyMatch(type::isAssignableFrom);
    }

    @SuppressWarnings("unchecked")
    public <T> T getAssignable(Class<T> type) {

        for (Map.Entry<Class<?>, Object> entry : services.entrySet()) {

            if (type.isAssignableFrom(entry.getKey())) {
                return (T) entry.getValue();
            }
        }

        return null;
    }
}