package com.carloship.craft.sdk.pubsub.serializer;

import com.carloship.craft.sdk.pubsub.message.OutgoingMessage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class KeyValueMessageSerializer implements MessageSerializer {

    @Override
    public <T> @NotNull String serialize(@NotNull OutgoingMessage<T> message) {
        if (!(message.data() instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("KeyValue serializer only supports Map<String,String>");
        }
        var sb = new StringBuilder();
        map.forEach((k, v) -> sb.append(k).append('=').append(v).append(';'));
        return sb.toString();
    }

    @Override
    public <T> @NotNull T deserialize(String raw, Class<T> type) {
        if (!Map.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException("KeyValue deserializer only supports Map types");
        }

        Map<String, String> map = new HashMap<>();

        if (raw == null || raw.isEmpty()) return (T) map;

        for (String token : raw.split(";")) {
            if (token.isBlank()) continue;
            String[] parts = token.split("=", 2);
            String key = parts[0];
            String value = parts.length > 1 ? parts[1] : "";
            map.put(key, value);
        }

        return (T) map;
    }
}