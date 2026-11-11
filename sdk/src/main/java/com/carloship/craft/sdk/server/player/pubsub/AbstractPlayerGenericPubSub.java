package com.carloship.craft.sdk.server.player.pubsub;

import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.pubsub.channel.AsyncPubSub;
import com.carloship.craft.sdk.pubsub.message.OutgoingMessage;
import com.carloship.craft.sdk.pubsub.middleware.MiddlewarePipeline;
import com.carloship.craft.sdk.pubsub.serializer.JsonMessageSerializer;
import com.carloship.craft.sdk.pubsub.serializer.MessageSerializer;
import com.carloship.craft.sdk.pubsub.topic.TopicRegistry;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class AbstractPlayerGenericPubSub {

    private final AsyncPubSub pubSub;
    private final AbstractRefreshTopic topic;

    public AbstractPlayerGenericPubSub(ModuleDependencyPacket packet, String channel, BiConsumer<UUID, String> consumer) {

        TopicRegistry registry = new TopicRegistry();
        MessageSerializer serializer = new JsonMessageSerializer(packet.getObjectMapper());
        MiddlewarePipeline pipeline = new MiddlewarePipeline();

        if (!channel.endsWith(":")) {
            channel += ":";
        }
        this.pubSub = new AsyncPubSub(packet.getPubSubAsyncCommands(), registry, serializer, pipeline);
        this.topic = new AbstractRefreshTopic(channel, consumer);

        pubSub.registerTopic(topic);
    }

    public CompletableFuture<Long> publishRefreshServerToProxy(UUID uuid, String target) {
        try {
            return pubSub.publish(topic, new OutgoingMessage<>(new AbstractRefreshPayload(uuid, "REFRESH", target)));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }


    public CompletableFuture<Long> publishRefreshProxyToServer(UUID uuid, String target) {
        try {
            return pubSub.publish(topic, new OutgoingMessage<>(new AbstractRefreshPayload(uuid, "REFRESH", target)));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
}