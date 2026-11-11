package com.carloship.craft.server.sheepwar;

import com.carloship.craft.sdk.cluster.model.NetworkServer;
import com.carloship.craft.sdk.module.SDKModule;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceFactory;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.server.sheepwar.lobby.SheepWarLobby;
import com.carloship.craft.server.sheepwar.player.SheepWarPlayer;
import com.carloship.craft.server.sheepwar.player.SheepWarPlayerModule;
import com.carloship.craft.server.sheepwar.room.SheepWarRoom;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.carloship.craft.server.SDKEnvironment.CLUSTER_ID;
import static com.carloship.craft.server.SDKEnvironment.SERVER_ID;

@Slf4j
@Getter
public class SheepWarServer extends SDKServer<SheepWarPlayer> {

    private final SheepWarsInstanceFactory factory;

    private SheepWarPlayerModule sheepWarPlayerModule;

    public SheepWarServer(MinecraftServer server, int port, String host) {
        super(server, port, host);
        this.factory = new SheepWarsInstanceFactory();
    }

    @Override
    public void onStart() {
        log.info("Starting Sheep Wars Server...");
        sheepWarPlayerModule = extractModule(SheepWarPlayerModule.class);

        SheepWarPlayerModule.hook();
        log.info("Started!...");
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public @NotNull NetworkServer getServerPayload() {
        String builtId = resolveServerId("sheep_wars_sv");

        return NetworkServer.builder()
                .serverId(SERVER_ID.getString(builtId))
                .clusterId(CLUSTER_ID.getString("local"))
                .gameId("sheep-wars")
                .serverType("dynamic")
                .port(getPort())
                .host(getHost())
                .maxPlayers(100)
                .canJoin(true)
                .build();
    }


    @Override
    public List<Class<? extends SDKModule>> serverModules() {
        return List.of(
                SheepWarPlayerModule.class
        );
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

    private static class SheepWarsInstanceFactory extends ServerInstanceFactory<SheepWarPlayer>{

        @Override
        public ServerInstance<SheepWarPlayer> createServer(ServerInstanceMode serverInstanceMode, SDKServer<SheepWarPlayer> root, boolean reply) {
            if (!(serverInstanceMode instanceof SheepWarsInstanceMode mode)) return null;
            return switch (mode){
                case LOBBY -> new SheepWarLobby(root);
                case ROOM -> new SheepWarRoom(root);
            };
        }

        @Override
        public ServerInstanceMode getMainMode() {
            return SheepWarsInstanceMode.LOBBY;
        }
    }
}
