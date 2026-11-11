package com.carloship.craft.sdk.pubsub.topic;

import com.carloship.craft.sdk.pubsub.message.IncomingMessage;
import lombok.Getter;

@Getter
public abstract class MessageTopic<T> {

    private final String name;
    private final Class<T> payloadType;

    protected MessageTopic(String name, Class<T> payloadType) {
        this.name = name;
        this.payloadType = payloadType;
    }

    public Class<T> payloadType() {
        return payloadType;
    }

    public abstract void onMessage(IncomingMessage<T> message) throws Exception;
}