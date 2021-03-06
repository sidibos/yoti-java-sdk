package com.yoti.api.client;

import java.util.Set;

/**
 * Represents a generic, typed key/value pair, a basic building block of
 * {@link Profile} instances.
 *
 */
public final class Attribute {
    private final String name;
    private final Object value;
    private final Set<String> sources;
    private final Set<String> verifiers;

    public Attribute(String name, Object value) {
        this(name, value, null);
    }
    
    public Attribute(String name, Object value, Set<String> sources) {
        this(name, value, sources, null);
    }

    public Attribute(String name, Object value, Set<String> sources, Set<String> verifiers) {
        this.name = name;
        this.value = value;
        this.sources = sources;
        this.verifiers = verifiers;
    }

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(Class<T> clazz) {
        return value != null ? (clazz.isAssignableFrom(value.getClass()) ? (T) value : null) : null;
    }

    public <T> T getValueOrDefault(Class<T> clazz, T defaultValue) {
        T result = getValue(clazz);
        return (result == null) ? defaultValue : result;
    }

    public Set<String> getSources() {
        return sources;
    }
    
    public Set<String> getVerifiers() {
        return verifiers;
    }
}
