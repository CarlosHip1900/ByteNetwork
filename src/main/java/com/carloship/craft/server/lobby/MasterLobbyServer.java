package com.carloship.craft.server.lobby;

import com.carloship.craft.sdk.cluster.model.NetworkServer;
import com.carloship.craft.sdk.module.SDKModule;
import com.carloship.craft.sdk.server.instance.ServerInstanceFactory;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.SDKServer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MasterLobbyServer extends SDKServer<BytePlayerCraft> {

    public MasterLobbyServer(MinecraftServer server, int port, String host) {
        super(server, port, host);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onShutdown() {

    }

    @Override
    public @NotNull NetworkServer getServerPayload() {
        return null;
    }

    @Override
    public List<Class<? extends SDKModule>> serverModules() {
        return List.of();
    }

    @Override
    public Class<? extends ServerInstanceMode> getEnumModes() {
        return null;
    }

    @Override
    protected BytePlayerCraft createPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        return null;
    }

    @Override
    protected ServerInstanceFactory<BytePlayerCraft> getServerFactory() {
        return null;
    }
}
