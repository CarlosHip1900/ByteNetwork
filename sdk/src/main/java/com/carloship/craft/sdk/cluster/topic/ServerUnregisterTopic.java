package com.carloship.craft.sdk.cluster.topic;

import com.carloship.craft.sdk.cluster.payload.ServerUnregisterPayload;
import com.carloship.craft.sdk.pubsub.message.IncomingMessage;
import com.carloship.craft.sdk.pubsub.topic.MessageTopic;

import java.util.function.Consumer;

public class ServerUnregisterTopic extends MessageTopic<ServerUnregisterPayload> {

    private final Consumer<ServerUnregisterPayload> consumer;

    public ServerUnregisterTopic(String channel, Consumer<ServerUnregisterPayload> consumer) {
        super(channel, ServerUnregisterPayload.class);
        this.consumer = consumer;
    }

    @Override
    public void onMessage(IncomingMessage<ServerUnregisterPayload> message) {
        if (message == null || message.data() == null) return;
        consumer.accept(message.data());
    }

}