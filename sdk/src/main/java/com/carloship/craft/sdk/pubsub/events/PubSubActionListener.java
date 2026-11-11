package com.carloship.craft.sdk.pubsub.events;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class PubSubActionListener<P extends IPubSubActionPayload> {

    private final List<IPubSubActionMiddleware<P>> middlewares = new CopyOnWriteArrayList<>();

    public void registerListener(IPubSubActionMiddleware<P> middleware) {
        middlewares.add(middleware);
    }

    public void completeAction(P payload) {
        for (IPubSubActionMiddleware<P> md : middlewares) {
            try {
                md.onReceivePayload(payload);
            } catch (Exception e) {
                log.error("Error in {}", getClass().getSimpleName(), e);
            }
        }
    }

}
