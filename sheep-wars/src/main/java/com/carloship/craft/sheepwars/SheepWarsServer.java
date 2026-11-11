package com.carloship.craft.sheepwars;

import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.sdk.SDKServerAPI;
import com.carloship.craft.sdk.cluster.model.NetworkServer;
import com.carloship.craft.sdk.module.SDKModule;
import com.carloship.craft.sdk.server.commands.LobbiesCommand;
import com.carloship.craft.sdk.server.commands.ServersMenuCommand;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceFactory;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sheepwars.instances.lobby.SheepWarLobby;
import com.carloship.craft.sheepwars.player.SheepWarPlayer;
import com.carloship.craft.sheepwars.player.SheepWarPlayerModule;
import com.carloship.craft.sheepwars.instances.room.SheepWarRoom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.carloship.craft.sdk.SDKEnvironment.CLUSTER_ID;
import static com.carloship.craft.sdk.SDKEnvironment.SERVER_ID;

@Slf4j
@Getter
public class SheepWarsServer extends SDKServer<SheepWarPlayer> {

    private final SheepWarsInstanceFactory factory;

    private SheepWarPlayerModule sheepWarPlayerModule;

    public SheepWarsServer(MinecraftServer server, int port, String host) {
        super(server, port, host);
        this.factory = new SheepWarsInstanceFactory();
    }

    @Override
    public void onStart() {
        log.info("Starting Sheep Wars Server...");
        sheepWarPlayerModule = extractModule(SheepWarPlayerModule.class);

        SheepWarPlayerModule.hook();

        MinecraftServer.getCommandManager()
                .register(
                        new ServersMenuCommand(),
                        new LobbiesCommand(SheepWarLobby.DEFAULT_SPAWN, SheepWarsInstanceMode.LOBBY)
                );

        log.info("Started!...");
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public @NotNull NetworkServer getServerPayload() {
        String builtId = resolveServerId(SDKServerAPI.SHEEP_WARS_SERVER_ID_PREFIX);

        return NetworkServer.builder()
                .serverId(SERVER_ID.getString(builtId))
                .clusterId(CLUSTER_ID.getString("local"))
                .gameId(SDKServerAPI.SHEEP_WARS_GAME_ID)
                .serverType(SDKServerAPI.MINIGAME_GAME_TYPE_ID)
                .port(getPort())
                .host(getHost())
                .maxPlayers(100)
                .canJoin(true)
                .build();
    }

    @Override
    public List<Class<? extends SDKModule>> serverModules() {
        return List.of(SheepWarPlayerModule.class);
    }

    @Override
    public Class<? extends ServerInstanceMode> getEnumModes() {
        return SheepWarsInstanceMode.class;
    }

    @Override
    protected SheepWarPlayer createPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        return new SheepWarPlayer(playerConnection, gameProfile);
    }

    @Override
    protected ServerInstanceFactory<SheepWarPlayer> getServerFactory() {
        return factory;
    }

    private static class SheepWarsInstanceFactory extends ServerInstanceFactory<SheepWarPlayer> {

        @Override
        public ServerInstance<SheepWarPlayer> createServer(ServerInstanceMode serverInstanceMode, SDKServer<SheepWarPlayer> root, boolean reply) {
            if (!(serverInstanceMode instanceof SheepWarsInstanceMode mode)) return null;

            if (mode == SheepWarsInstanceMode.LOBBY){
                return new SheepWarLobby(root);
            }

            return new SheepWarRoom(root, mode);
        }

        @Override
        public ServerInstanceMode getMainMode() {
            return SheepWarsInstanceMode.LOBBY;
        }
    }

}
