package com.carloship.craft.sdk.pubsub.serializer;

import com.carloship.craft.sdk.pubsub.message.OutgoingMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class JsonMessageSerializer implements MessageSerializer {

    private final ObjectMapper mapper;

    @Override
    public <T> String serialize(OutgoingMessage<T> message) throws Exception {
        return mapper.writeValueAsString(message.data());
    }

    @Override
    public <T> T deserialize(String raw, Class<T> type) throws Exception {
        return mapper.readValue(raw, type);
    }
}