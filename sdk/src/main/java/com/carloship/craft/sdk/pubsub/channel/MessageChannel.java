package com.carloship.craft.sdk.pubsub.channel;

import com.carloship.craft.sdk.pubsub.message.IncomingMessage;
import com.carloship.craft.sdk.pubsub.middleware.MiddlewarePipeline;
import com.carloship.craft.sdk.pubsub.serializer.MessageSerializer;
import com.carloship.craft.sdk.pubsub.topic.MessageTopic;
import com.carloship.craft.sdk.pubsub.topic.TopicRegistry;
import io.lettuce.core.pubsub.RedisPubSubListener;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter(AccessLevel.PROTECTED)
public abstract class MessageChannel implements RedisPubSubListener<String, String> {

    protected final StatefulRedisPubSubConnection<String, String> connection;
    protected final TopicRegistry registry;
    protected final MessageSerializer serializer;
    protected final MiddlewarePipeline pipeline;

    protected MessageChannel(@NotNull StatefulRedisPubSubConnection<String, String> connection,
                             TopicRegistry registry,
                             MessageSerializer serializer,
                             MiddlewarePipeline pipeline) {
        this.connection = connection;
        this.registry = registry;
        this.serializer = serializer;
        this.pipeline = pipeline;
        connection.addListener(this);
    }

    protected MessageChannel(@NotNull MessageChannel channel){
        this(channel.getConnection(), channel.getRegistry(), channel.getSerializer(), channel.getPipeline());
    }

    protected abstract void subscribe(String topicName);

    public void registerTopic(MessageTopic<?> topic) {
        registry.register(topic);
        subscribe(topic.getName());
    }

    @Override
    public void message(String channel, String message) {
        MessageTopic<?> topic = registry.get(channel);
        if (topic == null) return; // unknown topic

        try {

            Object data = serializer.deserialize(message, topic.payloadType());
            @SuppressWarnings("unchecked")
            IncomingMessage<Object> incoming = new IncomingMessage<>(message, data);

            // middleware pre
            incoming = pipeline.applyPre(incoming);

            // deliver
            @SuppressWarnings("unchecked")
            MessageTopic<Object> castTopic = (MessageTopic<Object>) topic;
            castTopic.onMessage(incoming);

            // middleware post
            pipeline.applyPost(incoming);

        } catch (Exception e) {
            System.err.printf("Failed to handle message for %s: %s\n", channel, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void message(String pattern, String channel, String message) {
    }

    @Override
    public void subscribed(String channel, long count) {

    }

    @Override
    public void psubscribed(String pattern, long count) {

    }

    @Override
    public void unsubscribed(String channel, long count) {

    }

    @Override
    public void punsubscribed(String pattern, long count) {

    }

}
