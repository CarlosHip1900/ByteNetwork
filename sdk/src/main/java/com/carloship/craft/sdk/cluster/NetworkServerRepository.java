package com.carloship.craft.sdk.cluster;

import com.carloship.craft.sdk.cluster.model.NetworkServer;
import com.carloship.craft.sdk.cluster.model.ProxiedPlayer;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class NetworkServerRepository {

    private final RedisCommands<String, String> sync;
    private final RedisAsyncCommands<String, String> async;

    public void registerServer(NetworkServer server) {
        String key = "network:server:" + server.getServerId();
        Map<String, String> fields = new HashMap<>();

        fields.put("serverId", nullSafe(server.getServerId()));
        fields.put("gameId", nullSafe(server.getGameId()));
        fields.put("clusterId", nullSafe(server.getClusterId()));
        fields.put("serverType", nullSafe(server.getServerType()));
        fields.put("host", nullSafe(server.getHost()));
        fields.put("players", String.valueOf(server.getPlayers()));
        fields.put("maxPlayers", String.valueOf(server.getMaxPlayers()));
        fields.put("canJoin", String.valueOf(server.isCanJoin()));
        fields.put("updatedAt", String.valueOf(System.currentTimeMillis()));
        fields.put("port", String.valueOf(server.getPort()));

        sync.hset(key, fields);
        sync.expire(key, 10);
        sync.sadd("network:servers:all", server.getServerId());
        sync.sadd("network:servers:game:" + server.getGameId(), server.getServerId());
        sync.sadd("network:servers:cluster:" + server.getClusterId(), server.getServerId());

        sync.sadd("network:games:all", server.getGameId());
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }

    public void heartbeat(NetworkServer server) {
        String key = "network:server:" + server.getServerId();
        sync.hset(key, Map.of(
                "players", String.valueOf(server.getPlayers()),
                "canJoin", String.valueOf(server.isCanJoin()),
                "updatedAt", String.valueOf(System.currentTimeMillis())
        ));
        sync.expire(key, 10);
    }

    public void unregisterServer(String serverId) {
        Map<String, String> data = sync.hgetall("network:server:" + serverId);
        sync.del("network:server:" + serverId);
        sync.srem("network:servers:all", serverId);
        if (data.containsKey("gameId")) {
            sync.srem("network:servers:game:" + data.get("gameId"), serverId);
            sync.srem("network:games:all:" + data.get("gameId"), serverId);
        }
        if (data.containsKey("clusterId"))
            sync.srem("network:servers:cluster:" + data.get("clusterId"), serverId);
    }

    public Optional<NetworkServer> getServer(String serverId) {
        Map<String, String> data = sync.hgetall("network:server:" + serverId);
        if (data == null || data.isEmpty()) return Optional.empty();
        return Optional.of(mapServer(data));
    }

    public List<NetworkServer> getServersByGame(String gameId) {
        return sync.smembers("network:servers:game:" + gameId).stream()
                .map(this::getServer)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Optional<NetworkServer> findAvailableServer(String gameId) {
        return getServersByGame(gameId).stream()
                .filter(NetworkServer::isCanJoin)
                .filter(s -> s.getPlayers() < s.getMaxPlayers())
                .min(Comparator.comparingInt(s -> s.getMaxPlayers() - s.getPlayers()));
    }

    public void trackPlayer(ProxiedPlayer player) {
        String key = "network:player:" + player.getUuid();
        sync.hset(key, Map.of(
                "uuid", player.getUuid().toString(),
                "username", player.getUsername(),
                "serverId", player.getServerId(),
                "gameId", player.getGameId(),
                "clusterId", player.getClusterId(),
                "joinedAt", String.valueOf(player.getJoinedAt())
        ));

        sync.set("network:players:index:" + player.getUsername(), player.getUuid().toString());
        sync.sadd("network:players:all", player.getUuid().toString());
        sync.sadd("network:players:server:" + player.getServerId(), player.getUuid().toString());
    }

    public void movePlayer(UUID uuid, String newServerId, String newGameId, String newClusterId) {
        String key = "network:player:" + uuid;
        String oldServerId = sync.hget(key, "serverId");
        if (oldServerId != null)
            sync.srem("network:players:server:" + oldServerId, uuid.toString());
        sync.hset(key, Map.of(
                "serverId", newServerId,
                "gameId", newGameId,
                "clusterId", newClusterId
        ));
        sync.sadd("network:players:server:" + newServerId, uuid.toString());
    }

    public void removePlayer(UUID uuid) {
        String key = "network:player:" + uuid;
        String serverId = sync.hget(key, "serverId");
        sync.del(key);
        sync.del("network:players:all", uuid.toString());
        sync.del("network:players:index:" + uuid.toString());
        if (serverId != null)
            sync.srem("network:players:server:" + serverId, uuid.toString());
    }

    public Optional<ProxiedPlayer> getPlayer(UUID uuid) {
        Map<String, String> data = sync.hgetall("network:player:" + uuid);
        if (data == null || data.isEmpty()) return Optional.empty();
        return Optional.of(mapPlayer(data));
    }

    public CompletableFuture<ProxiedPlayer> getPlayerAsyncFromUuid(UUID uuid) {
        return CompletableFuture.completedFuture(getPlayer(uuid).orElse(null));
    }

    public CompletableFuture<ProxiedPlayer> getPlayerAsyncFromName(String username) {
        return async.get("network:players:index:" + username).thenCompose(resultUuid -> {
            if (resultUuid == null) return CompletableFuture.completedFuture(null);
            UUID uuid = UUID.fromString(resultUuid);
            return getPlayerAsyncFromUuid(uuid);
        }).toCompletableFuture();
    }

    public Long getNetworkPlayerCount() {
        return sync.scard("network:players:all");
    }


    private NetworkServer mapServer(Map<String, String> d) {
        return NetworkServer.builder()
                .serverId(d.get("serverId"))
                .gameId(d.get("gameId"))
                .clusterId(d.get("clusterId"))
                .serverType(d.get("serverType"))
                .players(Integer.parseInt(d.getOrDefault("players", "0")))
                .maxPlayers(Integer.parseInt(d.getOrDefault("maxPlayers", "0")))
                .canJoin(Boolean.parseBoolean(d.getOrDefault("canJoin", "false")))
                .updatedAt(Long.parseLong(d.getOrDefault("updatedAt", "0")))
                .host(d.get("host"))
                .port(Integer.parseInt(d.get("port")))
                .build();
    }

    private ProxiedPlayer mapPlayer(Map<String, String> d) {
        ProxiedPlayer p = new ProxiedPlayer();
        p.setUuid(UUID.fromString(d.get("uuid")));
        p.setUsername(d.get("username"));
        p.setServerId(d.get("serverId"));
        p.setGameId(d.get("gameId"));
        p.setClusterId(d.get("clusterId"));
        p.setJoinedAt(Long.parseLong(d.getOrDefault("joinedAt", "0")));
        return p;
    }

    public List<NetworkServer> getServersByCluster(String clusterId) {
        return sync.smembers("network:servers:cluster:" + clusterId).stream()
                .map(this::getServer)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }


    public void incrementGamePlayers(String gameId) {
        async.incr("network:online:" + gameId);
    }

    public void decrementGamePlayers(String gameId) {
        async.decr("network:online:" + gameId);
    }

    public long getOnlinePlayersByGame(String gameId) {
        String value = sync.get("network:online:" + gameId);
        return value == null ? 0L : Long.parseLong(value);
    }

    public Map<String, Long> getOnlinePlayersAllGames() {
        Set<String> gameIds = sync.smembers("network:games:all");
        Map<String, Long> result = new LinkedHashMap<>();

        for (String gameId : gameIds) {
            String value = sync.get("network:online:" + gameId);
            result.put(gameId, value == null ? 0L : Long.parseLong(value));
        }

        return result;
    }
}