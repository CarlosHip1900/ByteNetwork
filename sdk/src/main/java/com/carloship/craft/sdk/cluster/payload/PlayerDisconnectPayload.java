package com.carloship.craft.sdk.cluster.payload;

import java.util.UUID;

public record PlayerDisconnectPayload(
    UUID uuid,
    String serverId
) implements NetworkPayload {}
