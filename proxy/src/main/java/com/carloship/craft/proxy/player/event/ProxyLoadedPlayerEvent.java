package com.carloship.craft.proxy.player.event;

import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.velocitypowered.api.proxy.Player;

public record ProxyLoadedPlayerEvent(
        Player proxiedPlayer,
        BytePlayerData networkPlayer
) { }
