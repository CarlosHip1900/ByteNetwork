package com.carloship.craft.proxy.player.event;

import com.carloship.craft.sdk.server.player.BytePlayerData;

import java.util.UUID;

public record ProxyLeavePlayerEvent(
        UUID uuid,
        BytePlayerData playerNetwork
) { }
