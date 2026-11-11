package com.carloship.craft.sdk.pubsub.message;

public record IncomingMessage<T>(String raw, T data) {
}