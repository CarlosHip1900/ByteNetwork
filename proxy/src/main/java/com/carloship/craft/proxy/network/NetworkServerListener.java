package com.carloship.craft.proxy.network;

import com.carloship.craft.proxy.Main;
import com.carloship.craft.proxy.player.event.TransferPlayerEvent;
import com.carloship.craft.proxy.whitelist.ProxyWhitelistCache;
import com.carloship.craft.sdk.cluster.NetworkProxyModule;
import com.carloship.craft.sdk.server.player.BytePlayerData;
import com.carloship.craft.sdk.server.player.BytePlayerModule;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static com.carloship.craft.sdk.utils.TextUtils.DMM;


@Slf4j
@RequiredArgsConstructor
public class NetworkServerListener {

    private final ProxyServer proxyServer;
    private final NetworkProxyModule proxyModule;
    private final BytePlayerModule networkModule;
    private final ProxyWhitelistCache whitelistCache;

    @Subscribe
    public void onTransfer(TransferPlayerEvent event) {
        Player player = event.player();
        RegisteredServer server = event.server();
        String serverId = event.serverId();

        proxyModule.getCachedServer(serverId).ifPresentOrElse(networkServer -> {
            BytePlayerData playerNetwork = networkModule.fetchCacheEntity(player.getUniqueId());

            if (whitelistCache.inWhitelist(networkServer.getGameId())) {
                if (whitelistCache.canJoin(networkServer.getGameId(), player.getUsername(), playerNetwork.getGroup())) {
                    sendConnection(player, server);
                    return;
                }

                player.sendMessage(DMM("<red>You not allowed connect to servers from this network..."));
                return;
            }

            sendConnection(player, server);
        }, () -> player.sendMessage(DMM("<red>We can't get about this server...")));
    }

    private void sendConnection(Player player, RegisteredServer server) {
        player.createConnectionRequest(server).connect().whenComplete((result, throwable) -> {
            if (throwable != null) {
                player.sendMessage(DMM("<red>Ops... we couldn't connect to this server"));
                log.error("Error try connect with server", throwable);
                return;
            }

            if (result.isSuccessful()) {
                player.sendMessage(DMM("<green>Connecting..."));
                return;
            }

            switch (result.getStatus()) {
                case ALREADY_CONNECTED -> player.sendMessage(DMM("<red>You already connected..."));
                case SERVER_DISCONNECTED -> player.sendMessage(DMM("<red>The server disconnected from the back-end"));
                case CONNECTION_CANCELLED -> player.sendMessage(DMM("<red>Back-end has terminated connection to this server"));
                case CONNECTION_IN_PROGRESS -> player.sendMessage(DMM("<red>The connection is already in progress..."));
                case SUCCESS -> player.sendMessage(DMM("<gray>Success..."));
            }
        });
    }

    @Subscribe
    public void onFirst(PlayerChooseInitialServerEvent event) {
        Player proxyPlayer = event.getPlayer();
        String gameId = NetworkServerDirection.fromPlayer(proxyPlayer.getUniqueId());
        if (gameId == null || gameId.isEmpty()) {
            log.error("ServerDirection gameId is null");
            event.getPlayer().disconnect(DMM("<red>We couldn't redirect you to some server"));
            return;
        }

        proxyModule.getRepository().findAvailableServer(gameId)
                .ifPresentOrElse(server -> {
                    proxyServer
                            .getServer(server.getServerId())
                            .ifPresentOrElse(
                                    event::setInitialServer,
                                    () -> event.getPlayer().disconnect(DMM("<red>We couldn't establish connection..."))
                            );
                }, () -> event.getPlayer().disconnect(DMM("<red>We can't find some server for send you to there!")));

    }

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        try {
            ServerPing originalPing = event.getPing();

            Optional<Favicon> favicon = Optional.empty();
            Path faviconPath = Main.getInstance().getDataDirectory().resolve("server-icon.png");
            if (Files.exists(faviconPath)) {
                try {
                    favicon = Optional.of(Favicon.create(faviconPath));
                } catch (IOException e) {
                    log.error("Error while get favicon ", e);
                }
            }

            if (favicon.orElse(null) == null) {
                log.error("Cannot use null favicon");
                return;
            }

            var settings = Main.getInstance().getSettings();

            ServerPing newPing = originalPing.asBuilder()
                    .description(settings.motd())
                    .maximumPlayers(settings.getMaxPlayers())
                    .favicon(favicon.orElse(null))
                    .build();

            event.setPing(newPing);

        } catch (Exception e) {
            log.error("Error when tried put new ping... ", e);
        }

    }
}
