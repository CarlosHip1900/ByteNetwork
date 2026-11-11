package com.carloship.craft.sdk.utils;

import java.util.*;
import java.util.function.Function;

public final class EnumQuery<K, E extends Enum<E>> {

    private final Map<K, E> lookup;

    public EnumQuery(E[] values, Function<E, K> keyMapper) {

        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(keyMapper, "keyMapper");

        Map<K, E> map = new LinkedHashMap<>();

        for (E value : values) {

            K key = keyMapper.apply(value);

            if (key == null) {
                throw new IllegalStateException("Enum key cannot be null for " + value.name());
            }

            E previous = map.put(key, value);

            if (previous != null) {
                throw new IllegalStateException("Duplicate enum key '" + key + "' for " + previous.name() + " and " + value.name());
            }
        }

        this.lookup = Collections.unmodifiableMap(map);
    }

    public E get(K key) {
        return lookup.get(key);
    }

    public E get(K key, E defaultValue) {
        return lookup.getOrDefault(key, defaultValue);
    }

    public E require(K key) {

        E value = lookup.get(key);

        if (value == null) {
            throw new IllegalArgumentException("Unknown enum key: " + key);
        }

        return value;
    }

    public boolean contains(K key) {
        return lookup.containsKey(key);
    }

    public Set<K> keys() {
        return lookup.keySet();
    }

    public Collection<E> values() {
        return lookup.values();
    }

    public int size() {
        return lookup.size();
    }

    public boolean isEmpty() {
        return lookup.isEmpty();
    }

    public Map<K, E> asMap() {
        return lookup;
    }

    @Override
    public String toString() {
        return "EnumQuery" + lookup;
    }

    public static <K, E extends Enum<E>> EnumQuery<K, E> of(
            E[] values,
            Function<E, K> keyMapper
    ) {
        return new EnumQuery<>(values, keyMapper);
    }

    public static <E extends Enum<E>> EnumQuery<String, E> of(
            E[] values
    ) {
        return new EnumQuery<>(values, Enum::name);
    }
}