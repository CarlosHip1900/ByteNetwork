package com.carloship.craft.sdk.server.player.loader;

import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.carloship.craft.sdk.utils.TextUtils;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.MinecraftServer;
import net.minestom.server.event.player.AsyncPlayerPreLoginEvent;
import net.minestom.server.network.player.PlayerConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;

@Slf4j
public class PlayerDataLoaderManager {

    private static final List<PlayerDataLoader> loaders = new ArrayList<>();

    public static void addLoader(PlayerDataLoader loader) {
        loaders.add(loader);
    }

    public void registerLoadListener(BytePlayerModule module) {
        MinecraftServer.getGlobalEventHandler().addListener(AsyncPlayerPreLoginEvent.class, event -> {
            PlayerConnection connection = event.getConnection();
            UUID playerUuid = event.getGameProfile().uuid();

            BytePlayerData playerData;
            try {
                playerData = module.fetchEntityAsync(playerUuid).get(600, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                connection.kick(TextUtils.INTERNAL_ERROR);
                PlayerDataLoaderProcessor.LOG.error("Error getting player network account {}", playerUuid, e);
                return;
            }

            if (playerData == null){
                connection.kick(DMM("<red>Could not process your data [packet-server]"));
                return;
            }

            PlayerDataLoaderProcessor.runAsyncAfterLogin(connection, "load account", (player, _) -> {
                log.info("BytePlayerData {} loaded...", player.getUsername());
                player.setPlayerData(playerData);
                log.info(playerData.toString());

                try {
                    CompletableFuture<?> chain = CompletableFuture.completedFuture(null);

                    for (PlayerDataLoader loader : loaders) {
                        chain = chain.thenCompose(v -> loader.loadData(player));
                    }

                    chain.get();

                } catch (Throwable t) {
                    connection.kick(TextUtils.INTERNAL_ERROR);
                    PlayerDataLoaderProcessor.LOG.error("Error getting player network account {}", playerUuid, t);
                }

            });
        });
    }


}
