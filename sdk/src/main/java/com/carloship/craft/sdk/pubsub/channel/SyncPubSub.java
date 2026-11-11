package com.carloship.craft.sdk.pubsub.channel;

import com.carloship.craft.sdk.pubsub.message.OutgoingMessage;
import com.carloship.craft.sdk.pubsub.middleware.MiddlewarePipeline;
import com.carloship.craft.sdk.pubsub.serializer.MessageSerializer;
import com.carloship.craft.sdk.pubsub.topic.MessageTopic;
import com.carloship.craft.sdk.pubsub.topic.TopicRegistry;
import io.lettuce.core.pubsub.api.sync.RedisPubSubCommands;
import org.jetbrains.annotations.NotNull;

public class SyncPubSub extends MessageChannel {

    private final RedisPubSubCommands<String,String> commands;

    public SyncPubSub(@NotNull RedisPubSubCommands<String,String> commands,
                      TopicRegistry registry,
                      MessageSerializer serializer,
                      MiddlewarePipeline pipeline) {
        super(commands.getStatefulConnection(), registry, serializer, pipeline);
        this.commands = commands;
    }

    @Override
    protected void subscribe(String topicName) { commands.subscribe(topicName); }

    public <T> void publish(@NotNull MessageTopic<T> topic, OutgoingMessage<T> outgoing) throws Exception {
        String raw = serializer.serialize(outgoing);
        commands.publish(topic.getName(), raw);
    }
}

