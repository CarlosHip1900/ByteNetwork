package com.carloship.craft.rankup;

import com.carloship.craft.rankup.player.RankupPlayer;
import com.carloship.craft.rankup.player.RankupPlayerModule;
import com.carloship.craft.sdk.SDKServer;
import com.carloship.craft.sdk.SDKServerAPI;
import com.carloship.craft.sdk.cluster.model.NetworkServer;
import com.carloship.craft.sdk.module.SDKModule;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceFactory;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import lombok.Getter;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.carloship.craft.sdk.SDKEnvironment.CLUSTER_ID;
import static com.carloship.craft.sdk.SDKEnvironment.SERVER_ID;

@Getter
public class RankupServer extends SDKServer<RankupPlayer> {

    private RankupPlayerModule rankupPlayerModule;

    public RankupServer(MinecraftServer server, int port, String host) {
        super(server, port, host);
    }

    @Override
    public void onStart() {
        this.rankupPlayerModule = extractModule(RankupPlayerModule.class);
    }

    @Override
    public void onShutdown() {

    }

    @Override
    public @NotNull NetworkServer getServerPayload() {
        String builtId = resolveServerId(SDKServerAPI.RANK_UP_SERVER_ID_PREFIX);

        return NetworkServer.builder()
                .serverId(SERVER_ID.getString(builtId))
                .clusterId(CLUSTER_ID.getString("local"))
                .gameId(SDKServerAPI.RANK_UP_GAME_ID)
                .serverType(SDKServerAPI.PERSISTENT_GAME_TYPE_ID)
                .port(getPort())
                .host(getHost())
                .maxPlayers(100)
                .canJoin(true)
                .build();
    }

    @Override
    public List<Class<? extends SDKModule>> serverModules() {
        return List.of(RankupPlayerModule.class);
    }

    @Override
    public Class<? extends ServerInstanceMode> getEnumModes() {
        return RankupInstanceMode.class;
    }

    @Override
    protected RankupPlayer createPlayer(PlayerConnection playerConnection, GameProfile gameProfile) {
        return new RankupPlayer(playerConnection, gameProfile);
    }

    @Override
    protected ServerInstanceFactory<RankupPlayer> getServerFactory() {
        return null;
    }

    private static final class RankupInstanceFactory extends ServerInstanceFactory<RankupPlayer>{

        @Override
        public ServerInstance<RankupPlayer> createServer(ServerInstanceMode serverInstanceMode, SDKServer<RankupPlayer> root, boolean reply) {
            if (!(serverInstanceMode instanceof RankupInstanceMode mode)) return null;

            return null;
        }

        @Override
        public ServerInstanceMode getMainMode() {
            return RankupInstanceMode.SPAWN;
        }
    }
}
