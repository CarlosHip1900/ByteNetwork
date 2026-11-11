package com.carloship.craft.sdk.server.instance;

import com.carloship.craft.sdk.SDKEnvironment;
import com.carloship.craft.sdk.SDKServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minestom.server.event.EventDispatcher;
import com.carloship.craft.sdk.server.instance.event.ServerInstanceDestroyEvent;
import com.carloship.craft.sdk.server.player.BytePlayerCraft;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class ServerInstanceManager<P extends BytePlayerCraft> {

    private static final int LIMIT = SDKEnvironment.LIMIT_INSTANCES.getInt(25);

    @Getter
    private final Map<UUID, ServerInstance<P>> lookup = new ConcurrentHashMap<>();
    private final Map<String, List<UUID>> instances = new ConcurrentHashMap<>();

    private final SDKServer<P> root;

    public ServerInstanceManager(SDKServer<P> server) {
        this.root = server;
    }

    public ServerInstance<P> getServerInstance(@NonNull UUID uuid) {
        return lookup.get(uuid);
    }

    public List<UUID> getInstancesFrom(@NonNull ServerInstanceMode mode) {
        return instances.getOrDefault(mode.getId(), Collections.emptyList());
    }

    public int getOnlinePlayers(ServerInstanceMode mode) {
        return getRunningInstances(mode)
                .stream()
                .mapToInt(ServerInstance<P>::getPlayerCount)
                .sum();
    }

    public List<ServerInstance<P>> getRunningInstances(@NonNull ServerInstanceMode mode) {
        return getInstancesFrom(mode).stream()
                .map(lookup::get)
                .filter(Objects::nonNull)
                .toList();
    }

    public Collection<ServerInstance<P>> getRunningInstances() {
        return lookup.values();
    }

    public ServerInstance<P> getInstanceFromAndById(@NonNull ServerInstanceMode mode, int id) {
        return getRunningInstances(mode).stream()
                .filter(instance -> instance.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public ServerInstance<P> createInstance(@NonNull ServerInstanceMode mode) {
        if (getInstancesFrom(mode).size() >= LIMIT) {
            throw new ServerInstanceException(
                    "Limite do modo " + mode.getId() + " foi atingido"
            );
        }

        boolean reply = !getInstancesFrom(mode).isEmpty();

        ServerInstance<P> instance = root.factoryServer(mode, reply);

        instance.setId(nextAvailableId(mode));

        addInstance(instance);

        instance.startServer();

        log.info("[CREATE] Instance {}={}/[{}]", mode, instance.getId(), instance.getServerId());
        return instance;
    }

    public void destroyInstance(@NonNull UUID uuid) {
        ServerInstance<P> instance = getServerInstance(uuid);
        if (instance == null) return;

        destroyInstance(instance);
    }

    public void destroyInstance(@NonNull ServerInstance<P> instance) {
        ServerInstance<P> lobbyServerInstance = findBest(root.getMainMode());
        if (lobbyServerInstance == null) {
            log.error("Cannot find main lobby");
            return;
        }

        EventDispatcher.call(new ServerInstanceDestroyEvent<>(instance, lobbyServerInstance));

        instances.get(instance.getInstanceMode().getId()).remove(instance.getServerId());
        lookup.remove(instance.getServerId());

        instance.stopServer();
    }

    public void addInstance(@NonNull ServerInstance<P> instance) {
        instances.computeIfAbsent(
                instance.getInstanceMode().getId(),
                k -> new ArrayList<>()
        ).add(instance.getServerId());

        lookup.put(instance.getServerId(), instance);
    }

    private int nextAvailableId(@NonNull ServerInstanceMode mode) {
        Set<Integer> used = getRunningInstances(mode).stream()
                .map(ServerInstance<P>::getId)
                .collect(Collectors.toSet());

        int id = 1;

        while (used.contains(id)) {
            id++;
        }

        return id;
    }

    public void removeInstance(@NonNull ServerInstance<P> instance) {
        List<UUID> uuids = instances.get(instance.getInstanceMode().getId());

        if (uuids != null) {
            uuids.remove(instance.getServerId());

            if (uuids.isEmpty()) {
                instances.remove(instance.getInstanceMode().getId());
            }
        }

        lookup.remove(instance.getServerId());
    }

    public ServerInstance<P> findBest(@NonNull ServerInstanceMode mode) {
        return getRunningInstances(mode).stream()
                .min(Comparator.comparingInt(ServerInstance<P>::getPlayerCount))
                .orElse(null);
    }
}