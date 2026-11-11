package com.carloship.craft.sdk.pubsub.events;

public interface IPubSubActionMiddleware<P extends IPubSubActionPayload> {

    void onReceivePayload(P payload);

}
