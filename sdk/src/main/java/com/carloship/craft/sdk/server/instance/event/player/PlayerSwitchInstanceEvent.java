package com.carloship.craft.sdk.server.instance.event.player;

import com.carloship.craft.sdk.server.instance.ServerInstance;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.Event;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;

public record PlayerSwitchInstanceEvent<P extends BytePlayerCraft>(
        P player,
        ServerInstance<P> oldInstance,
        ServerInstance<P> newInstance,
        Pos newInstancePos
) implements Event { }
