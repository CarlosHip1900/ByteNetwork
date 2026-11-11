package com.carloship.craft.sdk.pubsub.middleware;

import com.carloship.craft.sdk.pubsub.message.IncomingMessage;

import java.util.ArrayList;
import java.util.List;

public final class MiddlewarePipeline {

    private final List<MessageMiddleware> middlewares = new ArrayList<>();

    public void add(MessageMiddleware m) {
        middlewares.add(m);
    }

    public <T> IncomingMessage<T> applyPre(IncomingMessage<T> msg) throws Exception {
        IncomingMessage<T> current = msg;
        for (MessageMiddleware m : middlewares) {
            current = m.preHandle(current);
        }
        return current;
    }

    public <T> void applyPost(IncomingMessage<T> msg) throws Exception {
        for (MessageMiddleware m : middlewares) {
            m.postHandle(msg);
        }
    }
}