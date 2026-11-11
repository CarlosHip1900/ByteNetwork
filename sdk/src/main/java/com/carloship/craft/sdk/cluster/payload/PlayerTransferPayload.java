package com.carloship.craft.sdk.cluster.payload;

import java.util.UUID;

public record PlayerTransferPayload(
    UUID uuid,
    String targetServerId
) implements NetworkPayload {}
