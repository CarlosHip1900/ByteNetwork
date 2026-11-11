package com.carloship.craft.sdk.utils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class EnvVariable {

    private final String property;
    private final String environment;

    public String getString() {
        String v = System.getProperty(property);
        return (v == null || v.isBlank()) ? System.getenv(environment) : v;
    }

    public String getString(String def) {
        String v = getString();
        return (v == null || v.isBlank()) ? def : v.trim();
    }

    public int getInt() {
        try {
            return Integer.parseInt(getString());
        } catch (NumberFormatException ex) {
            log.error("Error while get int in property or environment", ex);
            return 0;
        }
    }

    public int getInt(int def) {
        try {
            return Integer.parseInt(getString());
        } catch (NumberFormatException ignored) {
            return def;
        }
    }

    public double getDouble() {
        try {
            return Double.parseDouble(getString());
        } catch (NumberFormatException ex) {
            log.error("Error while get double in property or environment", ex);
            return 0;
        }
    }

    public double getDouble(double def) {
        try {
            return Double.parseDouble(getString());
        } catch (NumberFormatException ignored) {
            return def;
        }
    }

    public float getFloat() {
        try {
            return Float.parseFloat(getString());
        } catch (NumberFormatException ex) {
            log.error("Error while get float in property or environment", ex);
            return 0;
        }
    }

    public float getFloat(float def) {
        try {
            return Float.parseFloat(getString());
        } catch (NumberFormatException ignored) {
            return def;
        }
    }

    public long getLong() {
        try {
            return Long.parseLong(getString());
        } catch (NumberFormatException ex) {
            log.error("Error while get long in property or environment", ex);
            return 0;
        }
    }

    public long getLong(long def) {
        try {
            return Long.parseLong(getString());
        } catch (NumberFormatException ignored) {
            return def;
        }
    }

    public boolean getBoolean() {
        try {
            return Boolean.parseBoolean(getString());
        } catch (NumberFormatException ex) {
            log.error("Error while get boolean in property or environment", ex);
            return false;
        }
    }

    public boolean getBoolean(boolean def) {
        try {
            return Boolean.parseBoolean(getString());
        } catch (NumberFormatException ignored) {
            return def;
        }
    }

    public <K, E extends Enum<E>> E getEnum(
            EnumQuery<K, E> query
    ) {

        String value = getString();

        if (value == null || value.isBlank()) {
            return null;
        }

        @SuppressWarnings("unchecked")
        K key = (K) value.trim();

        return query.get(key);
    }

    public <K, E extends Enum<E>> E getEnum(
            EnumQuery<K, E> query,
            E def
    ) {

        String value = getString();

        if (value == null || value.isBlank()) {
            return def;
        }

        try {

            @SuppressWarnings("unchecked")
            K key = (K) value.trim();

            return query.get(key, def);

        } catch (Exception ignored) {

            return def;
        }
    }
    
}
