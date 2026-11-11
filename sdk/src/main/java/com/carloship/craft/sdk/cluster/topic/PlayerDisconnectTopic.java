package com.carloship.craft.sdk.cluster.topic;

import com.carloship.craft.sdk.cluster.payload.PlayerDisconnectPayload;
import com.carloship.craft.sdk.pubsub.message.IncomingMessage;
import com.carloship.craft.sdk.pubsub.topic.MessageTopic;

import java.util.function.Consumer;

public class PlayerDisconnectTopic extends MessageTopic<PlayerDisconnectPayload> {

    private final Consumer<PlayerDisconnectPayload> consumer;

    public PlayerDisconnectTopic(String channel, Consumer<PlayerDisconnectPayload> consumer) {
        super(channel, PlayerDisconnectPayload.class);
        this.consumer = consumer;
    }

    @Override
    public void onMessage(IncomingMessage<PlayerDisconnectPayload> message) {
        if (message == null || message.data() == null) return;
        consumer.accept(message.data());
    }
}