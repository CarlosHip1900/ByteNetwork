package com.carloship.craft.sdk.cluster.payload;

public record ServerRegisterPayload(
    String serverId,
    String gameId,
    String clusterId,
    String serverType,
    int maxPlayers,
    String host,
    int port
) implements NetworkPayload {}
