package com.carloship.craft.sdk.server.instance.event;

import com.carloship.craft.sdk.server.instance.ServerInstance;
import net.minestom.server.event.Event;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

public record ServerInstanceDestroyEvent<P extends BytePlayerCraft>(
        ServerInstance<P> instance,
        ServerInstance<P> fallbackInstance
) implements Event { }
