package com.carloship.craft.sdk.cluster;

import com.carloship.craft.sdk.annotations.SDKModuleInfo;
import com.carloship.craft.sdk.cluster.model.NetworkServer;
import com.carloship.craft.sdk.cluster.payload.*;
import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.module.SDKModule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Getter
@SDKModuleInfo(
        id = "proxy",
        name = "Proxy",
        createDefault = false
)
public class NetworkProxyModule implements SDKModule {

    private final NetworkServerRepository repository;
    private final NetworkPubSub pubSub;

    private final Map<String, NetworkServer> serverCache = new ConcurrentHashMap<>();
    private final Map<Class<? extends NetworkPayload>, List<Consumer<NetworkPayload>>> executorsPayload = new ConcurrentHashMap<>();

    private ScheduledExecutorService healthCheckExecutor;

    @Setter
    private BiConsumer<UUID, String> transferConsumer;

    public NetworkProxyModule(ModuleDependencyPacket packet) {
        this.repository = new NetworkServerRepository(
                packet.getCommands(),
                packet.getAsyncCommands()
        );

        this.pubSub = new NetworkPubSub(
                packet,
                this::onServerRegister,
                this::onServerUnregister,
                this::onPlayerTransfer,
                this::onPlayerDisconnect
        );
    }

    @Override
    public void createModule() {
        startHealthCheck();
    }

    @Override
    public void disableModule() {
        stopHealthCheck();
    }

    private void onServerRegister(ServerRegisterPayload payload) {
        NetworkServer server = NetworkServer.builder()
                .serverId(payload.serverId())
                .gameId(payload.gameId())
                .clusterId(payload.clusterId())
                .serverType(payload.serverType())
                .maxPlayers(payload.maxPlayers())
                .players(0)
                .canJoin(true)
                .host(payload.host())
                .port(payload.port())
                .updatedAt(System.currentTimeMillis())
                .build();

        repository.registerServer(server);
        serverCache.put(server.getServerId(), server);

        log.info("[Network] Server registered: {} (game={}, cluster={})",
                payload.serverId(), payload.gameId(), payload.clusterId());

        execute(ServerRegisterPayload.class, payload);
    }

    private void onServerUnregister(ServerUnregisterPayload payload) {
        repository.unregisterServer(payload.serverId());
        serverCache.remove(payload.serverId());

        log.info("[Network] Server unregistered: {}", payload.serverId());

        execute(ServerUnregisterPayload.class, payload);
    }

    private void onPlayerTransfer(PlayerTransferPayload payload) {
        handlePlayerTransfer(payload.uuid(), payload.targetServerId());

        execute(PlayerTransferPayload.class, payload);
    }

    private void onPlayerDisconnect(PlayerDisconnectPayload payload) {
        repository.removePlayer(payload.uuid());
        repository.getPlayer(payload.uuid()).ifPresent(player ->
                repository.decrementGamePlayers(player.getGameId())
        );

        log.info("[Network] Player disconnected from network: {}", payload.uuid());

        execute(PlayerDisconnectPayload.class, payload);
    }

    private <E extends NetworkPayload> void execute(Class<E> payloadClass, E payload) {
        var list = executorsPayload.get(payloadClass);
        if (list == null || list.isEmpty()) return;
        list.forEach(networkPayloadConsumer -> networkPayloadConsumer.accept(payload));
    }

    public <E extends NetworkPayload> void addExecutorPayload(Class<E> payloadClass, Consumer<NetworkPayload> consumer) {
        executorsPayload.computeIfAbsent(payloadClass, l -> new ArrayList<>()).add(consumer);
    }

    public void handlePlayerTransfer(UUID uuid, String targetServerId) {
        NetworkServer target = serverCache.computeIfAbsent(
                targetServerId,
                id -> repository.getServer(id).orElse(null)
        );

        if (target == null) {
            log.warn("[Network] Transfer failed — server not found: {}", targetServerId);
            return;
        }

        if (!target.isCanJoin()) {
            log.warn("[Network] Transfer failed — server {} is not accepting players", targetServerId);
            return;
        }

        if (target.getPlayers() >= target.getMaxPlayers()) {
            log.warn("[Network] Transfer failed — server {} is full ({}/{})",
                    targetServerId, target.getPlayers(), target.getMaxPlayers());
            return;
        }

        repository.movePlayer(uuid, targetServerId, target.getGameId(), target.getClusterId());

        connectToServer(uuid, targetServerId);

        log.info("[Network] Transferred player {} to {}", uuid, targetServerId);
    }

    protected void connectToServer(UUID uuid, String targetServerId) {
        transferConsumer.accept(uuid, targetServerId);
    }


    public Optional<NetworkServer> getCachedServer(String serverId) {
        return Optional.ofNullable(serverCache.get(serverId));
    }

    public List<NetworkServer> getCachedServersByGame(String gameId) {
        return serverCache.values().stream()
                .filter(s -> gameId.equals(s.getGameId()))
                .collect(Collectors.toList());
    }

    public List<NetworkServer> getCachedServersByCluster(String clusterId) {
        return serverCache.values().stream()
                .filter(s -> clusterId.equals(s.getClusterId()))
                .collect(Collectors.toList());
    }

    public Collection<NetworkServer> getAllCachedServers() {
        return Collections.unmodifiableCollection(serverCache.values());
    }

    public CompletableFuture<Long> transferPlayer(UUID uuid, String targetServerId) {
        return pubSub.publishPlayerTransfer(uuid, targetServerId);
    }

    public Optional<NetworkServer> findAvailableServer(String gameId, String serverType) {
        return getCachedServersByGame(gameId).stream()
                .filter(s -> serverType.equals(s.getServerType()))
                .filter(NetworkServer::isCanJoin)
                .filter(s -> s.getPlayers() < s.getMaxPlayers())
                .min(Comparator.comparingInt(s -> s.getMaxPlayers() - s.getPlayers()));
    }

    private void startHealthCheck() {
        healthCheckExecutor = Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "network-proxy-healthcheck")
        );
        healthCheckExecutor.scheduleAtFixedRate(this::checkCachedServers, 10, 10, TimeUnit.SECONDS);
    }

    private void stopHealthCheck() {
        if (healthCheckExecutor != null) {
            healthCheckExecutor.shutdownNow();
        }
    }

    private void checkCachedServers() {
        if (serverCache.isEmpty()) return;

        for (String serverId : new HashSet<>(serverCache.keySet())) {
            Optional<NetworkServer> live = repository.getServer(serverId);

            if (live.isEmpty()) {
                serverCache.remove(serverId);
                log.warn("[Network] Server {} removed from cache — TTL expired (server is dead)", serverId);
                execute(ServerUnregisterPayload.class, new ServerUnregisterPayload(serverId, null, null));
            } else {
                serverCache.put(serverId, live.get());
            }
        }
    }
}