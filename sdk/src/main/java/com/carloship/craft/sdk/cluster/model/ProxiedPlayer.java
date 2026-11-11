package com.carloship.craft.sdk.cluster.model;

import lombok.Data;

import java.util.UUID;

@Data
public class ProxiedPlayer {

    private UUID uuid;

    private String username;

    private String serverId;

    private String gameId;

    private String clusterId;

    private long joinedAt;
}