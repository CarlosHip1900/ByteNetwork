package com.carloship.craft.sdk.mapper;

import com.carloship.craft.sdk.codec.ComponentCodec;
import com.carloship.craft.sdk.codec.LevelingCodec;
import com.carloship.craft.sdk.server.leveling.Leveling;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SDKJsonMapper {

    private static final Map<Class<?>, JsonSerializer<?>> SERIALIZERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, JsonDeserializer<?>> DESERIALIZERS = new ConcurrentHashMap<>();

    static {
        register(Component.class, new ComponentCodec.JSONSerializer(), new ComponentCodec.JSONDeserializer());
        register(Leveling.class, new LevelingCodec.JSONSerializer(), new LevelingCodec.JSONDeserializer());
    }

    public static @NonNull ObjectMapper create() {

        ObjectMapper mapper = new ObjectMapper();

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule module = new SimpleModule();

        SERIALIZERS.forEach((type, serializer) -> module.addSerializer((Class) type, serializer));
        DESERIALIZERS.forEach((type, deserializer) -> module.addDeserializer((Class) type, deserializer));

        mapper.registerModule(module);

        return mapper;
    }

    public static <T> void register(Class<T> type, JsonSerializer<T> serializer, JsonDeserializer<T> deserializer) {
        SERIALIZERS.put(type, serializer);
        DESERIALIZERS.put(type, deserializer);
    }
}