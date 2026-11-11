package com.carloship.craft.sdk.server.instance.event.player;

import com.carloship.craft.sdk.server.instance.ServerInstance;
import net.minestom.server.event.Event;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

public record PlayerInstanceQuitEvent<P extends BytePlayerCraft>(
        ServerInstance<P> instance,
        P player
) implements Event { }
