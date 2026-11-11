package com.carloship.craft.sdk.cluster.topic;

import com.carloship.craft.sdk.cluster.payload.PlayerTransferPayload;
import com.carloship.craft.sdk.pubsub.message.IncomingMessage;
import com.carloship.craft.sdk.pubsub.topic.MessageTopic;

import java.util.function.Consumer;

public class PlayerTransferTopic extends MessageTopic<PlayerTransferPayload> {

    private final Consumer<PlayerTransferPayload> consumer;

    public PlayerTransferTopic(String channel, Consumer<PlayerTransferPayload> consumer) {
        super(channel, PlayerTransferPayload.class);
        this.consumer = consumer;
    }

    @Override
    public void onMessage(IncomingMessage<PlayerTransferPayload> message) {
        if (message == null || message.data() == null) return;
        consumer.accept(message.data());
    }
}