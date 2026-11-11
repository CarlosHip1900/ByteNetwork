package com.carloship.craft.sdk.pubsub.events;

import com.carloship.craft.sdk.pubsub.message.IncomingMessage;
import com.carloship.craft.sdk.pubsub.topic.MessageTopic;

public class PubSubActionTopic<P extends IPubSubActionPayload> extends MessageTopic<P> {

    private final PubSubActionListener<P> listener;

    public PubSubActionTopic(String name, Class<P> payloadType, PubSubActionListener<P> listener) {
        super(name, payloadType);
        this.listener = listener;
    }

    @Override
    public void onMessage(IncomingMessage<P> message) throws Exception {
        if (message == null) return;
        if (message.data() == null) return;;
        listener.completeAction(message.data());
    }
}
