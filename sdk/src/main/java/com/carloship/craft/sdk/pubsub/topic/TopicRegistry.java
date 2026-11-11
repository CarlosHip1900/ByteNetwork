package com.carloship.craft.sdk.pubsub.topic;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TopicRegistry {

    private final Map<String, MessageTopic<?>> topics = new ConcurrentHashMap<>();

    public void register(MessageTopic<?> topic) {
        topics.put(topic.getName(), topic);
    }

    public MessageTopic<?> get(String name) {
        return topics.get(name);
    }

    public Collection<MessageTopic<?>> all() {
        return topics.values();
    }
}
