package com.carloship.craft.sdk;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import net.minestom.server.MinecraftServer;

@FunctionalInterface
public interface ServerFactory<P extends BytePlayerCraft> {

    SDKServer<P> create(MinecraftServer minecraftServer, int port, String host);

}