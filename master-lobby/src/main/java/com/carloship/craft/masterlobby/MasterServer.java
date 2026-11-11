package com.carloship.craft.masterlobby;

import com.carloship.craft.sdk.server.commands.LobbiesCommand;
import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.sdk.SDKServerAPI;
import com.carloship.craft.sdk.SDKServerType;
import com.carloship.craft.sdk.cluster.model.NetworkServer;
import com.carloship.craft.sdk.module.SDKModule;
import com.carloship.craft.sdk.server.commands.ServersMenuCommand;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceFactory;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.carloship.craft.sdk.SDKEnvironment.CLUSTER_ID;
import static com.carloship.craft.sdk.SDKEnvironment.SERVER_ID;

@Slf4j
public class MasterServer extends SDKServer<BytePlayerCraft> {

    public MasterServer(MinecraftServer server, int port, String host) {
        super(server, port, host);
    }

    @Override
    public void onStart() {
        log.info("Started LobbyMaster server!");

        MinecraftServer.getCommandManager()
                .register(
                        new LobbiesCommand(MasterInstance.DEFAULT_SPAWN, SDKServerType.MASTER_LOBBY),
                        new ServersMenuCommand()
                );
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public @NotNull NetworkServer getServerPayload() {
        String builtId = resolveServerId(SDKServerAPI.MASTER_LOBBY_SERVER_ID_PREFIX);

        return NetworkServer.builder()
                .serverId(SERVER_ID.getString(builtId))
                .clusterId(CLUSTER_ID.getString("local"))
                .gameId(SDKServerAPI.MASTER_LOBBY_GAME_ID)
                .serverType(SDKServerAPI.PERSISTENT_GAME_TYPE_ID)
                .port(getPort())
                .host(getHost())
                .maxPlayers(100)
                .canJoin(true)
                .build();
    }

    @Override
    public List<Class<? extends SDKModule>> serverModules() {
        return List.of();
    }

    @Override
    public Class<? extends ServerInstanceMode> getEnumModes() {
        return SDKServerType.class;
    }

    @Override
    protected BytePlayerCraft createPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        return new BytePlayerCraft(playerConnection, gameProfile);
    }

    @Override
    protected ServerInstanceFactory<BytePlayerCraft> getServerFactory() {
        return new ServerInstanceFactory<>() {

            @Override
            public ServerInstance<BytePlayerCraft> createServer(ServerInstanceMode mode, SDKServer<BytePlayerCraft> root, boolean reply) {
                return new MasterInstance(root);
            }

            @Override
            public ServerInstanceMode getMainMode() {
                return SDKServerType.MASTER_LOBBY;
            }
        };
    }

}
