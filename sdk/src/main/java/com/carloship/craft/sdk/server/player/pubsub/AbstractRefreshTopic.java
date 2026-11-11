package com.carloship.craft.sdk.server.player.pubsub;

import com.carloship.craft.sdk.pubsub.message.IncomingMessage;
import com.carloship.craft.sdk.pubsub.topic.MessageTopic;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.function.BiConsumer;

@Slf4j
public class AbstractRefreshTopic extends MessageTopic<AbstractRefreshPayload> {

    private final BiConsumer<UUID, String> consumer;

    public AbstractRefreshTopic(String channel, BiConsumer<UUID, String> consumer) {
        super(channel, AbstractRefreshPayload.class);

        this.consumer = consumer;
    }

    @Override
    public void onMessage(IncomingMessage<AbstractRefreshPayload> message) {

        if (message == null || message.data() == null) {
            return;
        }

        AbstractRefreshPayload data = message.data();

        consumer.accept(data.uuid(), data.target());
    }
}