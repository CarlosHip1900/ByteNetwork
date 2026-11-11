package com.carloship.craft.sdk;

import com.carloship.craft.sdk.cluster.NetworkModule;
import com.carloship.craft.sdk.server.common.GameHotBarManager;
import com.carloship.craft.sdk.server.instance.ServerInstance;
import com.carloship.craft.sdk.server.instance.ServerInstanceMode;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.utils.Pair;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class SDKServerAPI {

    public static final String MASTER_LOBBY_GAME_ID = "master_lobby";
    public static final String MASTER_LOBBY_SERVER_ID_PREFIX = makeIdPrefix(MASTER_LOBBY_GAME_ID);

    public static final String SHEEP_WARS_GAME_ID = "sheep_wars";
    public static final String SHEEP_WARS_SERVER_ID_PREFIX = makeIdPrefix(SHEEP_WARS_GAME_ID);

    public static final String RANK_UP_GAME_ID = "rank_up";
    public static final String RANK_UP_SERVER_ID_PREFIX = makeIdPrefix(RANK_UP_GAME_ID);

    public static final String MINIGAME_GAME_TYPE_ID = "minigame";
    public static final String PERSISTENT_GAME_TYPE_ID = "persistent";

    public static final String SHOP_LINK_WEBSITE_ID = "Coming soon...";

    private static String makeIdPrefix(String v) {
        return v + "_sv";
    }

    public static final Pair<Component, Component> DEFAULT_TAB_LIST = new Pair<>(
            DMM("").appendNewline().append(DMM("<gradient:#00FFFF:#87CEFA><bold>BYTE NETWORK</bold></gradient>")).appendNewline(),
            DMM("").appendNewline().append(DMM("<aqua>Ranks and Bits on <gold>" + SDKServerAPI.SHOP_LINK_WEBSITE_ID))
    );

    public static List<ServerInstance<BytePlayerCraft>> runningInstancesFrom(SDKServerType type) {
        return SDKServer.getTypedInstance()
                .getInstanceManager()
                .getRunningInstances(type);
    }

    public static List<ServerInstance<BytePlayerCraft>> runningInstancesFrom(ServerInstanceMode type) {
        return SDKServer.getTypedInstance()
                .getInstanceManager()
                .getRunningInstances(type);
    }


    public static ServerInstance<BytePlayerCraft> instanceFrom(BytePlayerCraft player) {
        return SDKServer.getTypedInstance()
                .getPlayerInstance(player);
    }

    public static GameHotBarManager hotBarManager() {
        return SDKServer.getTypedInstance().getHotBarManager();
    }

    public static BytePlayerModule bytePlayerModule() {
        return SDKServer.getTypedInstance().getBytePlayerModule();
    }

    public static NetworkModule networkModule() {
        return SDKServer.getTypedInstance().getNetworkMinestomModule();
    }

    public static CompletableFuture<?> sendPlayerToGame(BytePlayerCraft playerCraft, SDKServerType type) {
        return networkModule().transferPlayerToGame(playerCraft.getUuid(), type.getGameId())
                .whenComplete((l, throwable) -> {
                    if (throwable != null) {
                        playerCraft.sendErrorMessage("Ops... A problem occurred while tried transfer you to another server...");
                        log.error("Error transfer player", throwable);
                        return;
                    }
                });
    }

    public static SDKServerType serverType() {
        return SDKServer.getTypedInstance().getServerType();
    }

    public static long getOnlinePlayers(SDKServerType sdkServerType) {
        return SDKServer.getTypedInstance().countPlayersInServer(sdkServerType.getGameId());
    }
}
