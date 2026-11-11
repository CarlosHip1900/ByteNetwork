package com.carloship.craft.sdk.cluster.topic;

import com.carloship.craft.sdk.cluster.payload.ServerRegisterPayload;
import com.carloship.craft.sdk.pubsub.message.IncomingMessage;
import com.carloship.craft.sdk.pubsub.topic.MessageTopic;

import java.util.function.Consumer;

public class ServerRegisterTopic extends MessageTopic<ServerRegisterPayload> {

    private final Consumer<ServerRegisterPayload> consumer;

    public ServerRegisterTopic(String channel, Consumer<ServerRegisterPayload> consumer) {
        super(channel, ServerRegisterPayload.class);
        this.consumer = consumer;
    }

    @Override
    public void onMessage(IncomingMessage<ServerRegisterPayload> message) {
        if (message == null || message.data() == null) return;
        consumer.accept(message.data());
    }
}