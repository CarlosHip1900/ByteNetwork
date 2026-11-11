package com.carloship.craft.sdk.cluster.payload;

public record ServerUnregisterPayload(
    String serverId,
    String gameId,
    String clusterId
) implements NetworkPayload {}
