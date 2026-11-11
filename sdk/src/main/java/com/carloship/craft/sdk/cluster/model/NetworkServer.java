package com.carloship.craft.sdk.cluster.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NetworkServer {

    private String serverId;

    private String gameId;

    private String clusterId;

    private String serverType;

    private String host;

    private int port;

    private int players;

    private int maxPlayers;

    private boolean canJoin;

    private long updatedAt;

}