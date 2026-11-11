package com.carloship.craft.sdk.server.instance;

import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.SDKServer;

public abstract class ServerInstanceFactory<P extends BytePlayerCraft> {

    public abstract ServerInstance<P> createServer(ServerInstanceMode mode, SDKServer<P> root, boolean reply);

    public abstract ServerInstanceMode getMainMode();
}
