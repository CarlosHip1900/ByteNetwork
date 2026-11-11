package com.carloship.craft.proxy.player.event;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public record TransferPlayerEvent(
        Player player,
        RegisteredServer server,
        String serverId
) { }
