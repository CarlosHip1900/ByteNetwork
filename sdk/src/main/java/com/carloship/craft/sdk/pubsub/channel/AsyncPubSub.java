package com.carloship.craft.sdk.pubsub.channel;

import com.carloship.craft.sdk.pubsub.message.OutgoingMessage;
import com.carloship.craft.sdk.pubsub.middleware.MiddlewarePipeline;
import com.carloship.craft.sdk.pubsub.serializer.MessageSerializer;
import com.carloship.craft.sdk.pubsub.topic.MessageTopic;
import com.carloship.craft.sdk.pubsub.topic.TopicRegistry;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class AsyncPubSub extends MessageChannel {

    private final RedisPubSubAsyncCommands<String, String> commands;

    public AsyncPubSub(@NotNull RedisPubSubAsyncCommands<String, String> commands,
                       TopicRegistry registry,
                       MessageSerializer serializer,
                       MiddlewarePipeline pipeline) {
        super(commands.getStatefulConnection(), registry, serializer, pipeline);
        this.commands = commands;
    }

    @Override
    protected void subscribe(String topicName) {
        commands.subscribe(topicName);
    }

    public <T> CompletableFuture<Long> publish(@NotNull MessageTopic<T> topic, OutgoingMessage<T> outgoing) throws Exception {
        String raw = serializer.serialize(outgoing);
        return commands.publish(topic.getName(), raw).toCompletableFuture();
    }

}

