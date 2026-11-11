package com.carloship.craft.sdk.utils;

import org.jetbrains.annotations.NotNull;

public record Pair<K, V>(@NotNull K key, @NotNull V value) {

    @NotNull
    public K left() {
        return key;
    }

    @NotNull
    public V right(){
        return value;
    }

}