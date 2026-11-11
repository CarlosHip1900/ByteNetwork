package com.carloship.craft.sdk.cluster;

import com.carloship.craft.sdk.annotations.SDKModuleInfo;
import com.carloship.craft.sdk.cluster.model.NetworkServer;
import com.carloship.craft.sdk.cluster.model.ProxiedPlayer;
import com.carloship.craft.sdk.cluster.payload.ServerRegisterPayload;
import com.carloship.craft.sdk.cluster.payload.ServerUnregisterPayload;
import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.module.SDKModule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
@SDKModuleInfo(
        id = "minestom",
        name = "Minestom",
        createDefault = false
)
public class NetworkModule implements SDKModule {

    private final NetworkServerRepository repository;
    private final NetworkPubSub pubSub;
    private final NetworkServer self;

    private ScheduledExecutorService heartbeatExecutor;

    public NetworkModule(ModuleDependencyPacket packet, NetworkServer self) {
        this.self = self;
        this.repository = new NetworkServerRepository(
                packet.getCommands(),
                packet.getAsyncCommands()
        );

        this.pubSub = new NetworkPubSub(
                packet,
                payload -> {
                },
                payload -> {
                },
                payload -> {
                },
                payload -> {
                }
        );
    }

    public void register() {
        repository.registerServer(self);

        pubSub.publishServerRegister(new ServerRegisterPayload(
                self.getServerId(),
                self.getGameId(),
                self.getClusterId(),
                self.getServerType(),
                self.getMaxPlayers(),
                self.getHost(),
                self.getPort()
        ));

        startHeartbeat();

        log.info("[Network] Server {} registered to network", self.getServerId());
    }

    public void unregister() {
        stopHeartbeat();

        repository.unregisterServer(self.getServerId());

        pubSub.publishServerUnregister(new ServerUnregisterPayload(
                self.getServerId(),
                self.getGameId(),
                self.getClusterId()
        ));

        log.info("[Network] Server {} unregistered from network", self.getServerId());
    }

    private void startHeartbeat() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "network-heartbeat")
        );
        heartbeatExecutor.scheduleAtFixedRate(this::heartbeat, 0, 5, TimeUnit.SECONDS);
    }

    private void stopHeartbeat() {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdownNow();
        }
    }

    private void heartbeat() {
        try {
            self.setUpdatedAt(System.currentTimeMillis());
            repository.heartbeat(self);
        } catch (Exception e) {
            log.error("[Network] Heartbeat failed for server {}", self.getServerId(), e);
        }
    }

    public CompletableFuture<ProxiedPlayer> fetchPlayer(UUID uuid){
        return repository.getPlayerAsyncFromUuid(uuid);
    }

    public CompletableFuture<ProxiedPlayer> fetchPlayer(String username){
        return repository.getPlayerAsyncFromName(username);
    }

    public void trackPlayerJoin(UUID uuid, String username) {
        ProxiedPlayer player = new ProxiedPlayer();
        player.setUuid(uuid);
        player.setUsername(username);
        player.setServerId(self.getServerId());
        player.setGameId(self.getGameId());
        player.setClusterId(self.getClusterId());
        player.setJoinedAt(System.currentTimeMillis());

        repository.trackPlayer(player);
        repository.incrementGamePlayers(self.getGameId());

        self.setPlayers(self.getPlayers() + 1);

        log.info("[Network] Player {} joined server {}", username, self.getServerId());
    }

    public void trackPlayerLeave(UUID uuid) {
        repository.removePlayer(uuid);
        repository.decrementGamePlayers(self.getGameId());

        pubSub.publishPlayerDisconnect(uuid, self.getServerId());

        self.setPlayers(Math.max(0, self.getPlayers() - 1));

        log.info("[Network] Player {} left server {}", uuid, self.getServerId());
    }

    public Optional<NetworkServer> findAvailableServer(String gameId) {
        return repository.findAvailableServer(gameId);
    }

    public Optional<NetworkServer> findAvailableServerExcludingSelf(String gameId) {
        return repository.getServersByGame(gameId).stream()
                .filter(s -> !s.getServerId().equals(self.getServerId()))
                .filter(NetworkServer::isCanJoin)
                .filter(s -> s.getPlayers() < s.getMaxPlayers())
                .min(Comparator.comparingInt(s -> s.getMaxPlayers() - s.getPlayers()));
    }

    public List<NetworkServer> getServersInCluster(String clusterId) {
        return repository.getServersByCluster(clusterId);
    }

    public CompletableFuture<Long> transferPlayer(UUID uuid, String targetServerId) {
        log.info("Sending {} to {}", uuid, targetServerId);
        return pubSub.publishPlayerTransfer(uuid, targetServerId);
    }

    public CompletableFuture<Long> transferPlayerToGame(UUID uuid, String gameId) {
        return findAvailableServer(gameId)
                .map(server -> transferPlayer(uuid, server.getServerId()))
                .orElseGet(() -> {
                    log.warn("[Network] No available server found for game {}", gameId);
                    return CompletableFuture.failedFuture(
                            new IllegalStateException("No available server for game: " + gameId)
                    );
                });
    }

    public Optional<NetworkServer> findAvailableServer(String gameId, String serverType) {
        return repository.getServersByGame(gameId).stream()
                .filter(s -> serverType.equals(s.getServerType()))
                .filter(NetworkServer::isCanJoin)
                .filter(s -> s.getPlayers() < s.getMaxPlayers())
                .min(Comparator.comparingInt(s -> s.getMaxPlayers() - s.getPlayers()));
    }

    public CompletableFuture<Long> transferPlayerToDimension(UUID uuid, String gameId, String serverType) {
        return findAvailableServer(gameId, serverType)
                .map(server -> transferPlayer(uuid, server.getServerId()))
                .orElseGet(() -> {
                    log.warn("[Network] No {} {} server available for player {}", gameId, serverType, uuid);
                    return CompletableFuture.failedFuture(
                            new IllegalStateException("No server available: " + gameId + ":" + serverType)
                    );
                });
    }

    public void closeServer() {
        self.setCanJoin(false);
        log.info("[Network] Server {} marked as closed", self.getServerId());
    }

    public void openServer() {
        self.setCanJoin(true);
        log.info("[Network] Server {} marked as open", self.getServerId());
    }

    public int getOnlinePlayers() {
        return self.getPlayers();
    }

    public boolean isFull() {
        return self.getPlayers() >= self.getMaxPlayers();
    }

    @Override
    public void createModule() {
        heartbeat();
    }

    @Override
    public void disableModule() {

    }
}