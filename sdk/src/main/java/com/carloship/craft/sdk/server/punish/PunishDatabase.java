package com.carloship.craft.sdk.server.punish;

import com.carloship.craft.sdk.database.cloud.MongoDatabase;
import com.carloship.craft.sdk.database.cloud.RedisDatabase;
import com.carloship.craft.sdk.server.punish.data.PunishEntry;
import com.carloship.craft.sdk.server.punish.data.PunishType;
import com.carloship.craft.sdk.server.punish.data.RevokeEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.Indexes;
import io.lettuce.core.RedisClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class PunishDatabase {

    private final RedisDatabase<PunishEntry> punishRedis;
    private final RedisDatabase<RevokeEntry> revokeRedis;

    private final MongoDatabase<PunishEntry> punishMongo;
    private final MongoDatabase<RevokeEntry> revokeMongo;

    private final Cache<UUID, PunishEntry> punishedPlayers;
    private final Cache<UUID, PunishEntry> mutedPlayers;

    public PunishDatabase(RedisClient client, ObjectMapper objectMapper, MongoClient mongoClient) {
        this.punishRedis = new RedisDatabase<>(client, PunishEntry.class, objectMapper);
        this.revokeRedis = new RedisDatabase<>(client, RevokeEntry.class, objectMapper);
        this.punishMongo = new MongoDatabase<>(mongoClient, "byte-network", PunishEntry.class, "punish");
        this.revokeMongo = new MongoDatabase<>(mongoClient, "byte-network", RevokeEntry.class, "revoke");

        this.punishedPlayers = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).build();
        this.mutedPlayers = Caffeine.newBuilder().expireAfterAccess(Duration.ofMinutes(10)).build();

        punishMongo.indexes(
                new IndexModel(Indexes.compoundIndex(Indexes.ascending("userUuid"), Indexes.descending("createdAt"))),
                new IndexModel(Indexes.compoundIndex(Indexes.ascending("userUuid"), Indexes.ascending("reason"))),
                new IndexModel(Indexes.ascending("ipAddress"))
        );
        revokeMongo.indexes(
                new IndexModel(Indexes.ascending("punishObject.userUuid"))
        );
    }


    public void uncachePlayer(UUID uuid) {
        punishedPlayers.invalidate(uuid);
        mutedPlayers.invalidate(uuid);
    }

    public void uncachePlayerMute(UUID uuid) {
        mutedPlayers.invalidate(uuid);
    }

    public PunishEntry getCachedMute(UUID uuid) {
        return mutedPlayers.getIfPresent(uuid);
    }

    public CompletableFuture<Boolean> savePunish(PunishEntry punish) {
        UUID uuid = punish.getUserUuid();

        punishedPlayers.put(uuid, punish);
        if (punish.getType() == PunishType.MUTE) {
            mutedPlayers.put(uuid, punish);
        }

        CompletableFuture<Boolean> redisSave = CompletableFuture.allOf(
                punishRedis.saveAsync(latestPunishKey(uuid), punish, Duration.ofMinutes(10)),
                punishRedis.saveAsync(punishKey(uuid, punish.getId()), punish, Duration.ofMinutes(10))
        ).thenApply(v -> true).exceptionally(ex -> {
            log.warn("Failed to cache punish in Redis for user {}", uuid, ex);
            return false;
        });

        return redisSave.thenCompose(v -> punishMongo.saveAsync(punish));
    }

    public CompletableFuture<PunishEntry> getLastPlayerPunish(UUID playerUuid) {
        var cached = punishedPlayers.getIfPresent(playerUuid);
        if (cached != null) {
            if (cached.isPassed()) return CompletableFuture.completedFuture(null);
            if (!cached.isActive()) return CompletableFuture.completedFuture(null);
            return CompletableFuture.completedFuture(cached);
        }

        return punishRedis.fetchAsync(latestPunishKey(playerUuid))
                .exceptionally(ex -> {
                    log.warn("Redis fetch error for user {}, falling back to Mongo", playerUuid);
                    return null;
                })
                .thenCompose(redisV -> {
                    if (redisV != null) {
                        if (redisV.isPassed()) return CompletableFuture.completedFuture(null);
                        if (!redisV.isActive()) return CompletableFuture.completedFuture(null);

                        cacheLocally(playerUuid, redisV);
                        return CompletableFuture.completedFuture(redisV);
                    }

                    return punishMongo.fetchAsyncFilter(Filters.eq("userUuid", playerUuid))
                            .thenApply(mongoV -> {
                                if (mongoV != null) {
                                    if (mongoV.isPassed()) return null;
                                    if (!mongoV.isActive()) return null;

                                    cacheLocally(playerUuid, mongoV);
                                    punishRedis.saveAsync(latestPunishKey(playerUuid), mongoV, Duration.ofMinutes(10));
                                    punishRedis.saveAsync(punishKey(playerUuid, mongoV.getId()), mongoV, Duration.ofMinutes(10));
                                }
                                return mongoV;
                            });
                });
    }

    public CompletableFuture<Boolean> saveRevoke(RevokeEntry revoke) {
        UUID userUuid = revoke.getPunishObject().getUserUuid();

        uncachePlayer(userUuid);
        punishRedis.deleteAsync(latestPunishKey(userUuid));

        return revokeRedis.saveAsync(revokeKey(userUuid, revoke.getId()), revoke, Duration.ofMinutes(10))
                .thenCompose(v -> revokeMongo.saveAsync(revoke));
    }

    public CompletableFuture<RevokeEntry> getPlayerRevoke(UUID playerUuid, UUID revokeUuid) {
        return revokeRedis.fetchAsync(revokeKey(playerUuid, revokeUuid))
                .exceptionally(ex -> null)
                .thenCompose(redisV -> {
                    if (redisV != null) return CompletableFuture.completedFuture(redisV);

                    var filter = Filters.and(
                            Filters.eq("punishObject.userUuid", playerUuid),
                            Filters.eq("_id", revokeUuid)
                    );

                    return revokeMongo.fetchAsyncFilter(filter).thenApply(mongoV -> {
                        if (mongoV != null) {
                            revokeRedis.saveAsync(revokeKey(playerUuid, mongoV.getId()), mongoV, Duration.ofMinutes(10));
                        }
                        return mongoV;
                    });
                });
    }

    public CompletableFuture<Optional<PunishEntry>> getActiveMute(UUID playerUuid) {
        PunishEntry cached = mutedPlayers.getIfPresent(playerUuid);
        if (isPunishmentActive(cached)) {
            return CompletableFuture.completedFuture(Optional.of(cached));
        }

        return getLastPlayerPunish(playerUuid).thenApply(punish -> {
            if (punish != null && punish.getType() == PunishType.MUTE && isPunishmentActive(punish)) {
                mutedPlayers.put(playerUuid, punish);
                return Optional.of(punish);
            }
            return Optional.empty();
        });
    }

    public CompletableFuture<Optional<PunishEntry>> getActiveBan(UUID playerUuid) {
        return getLastPlayerPunish(playerUuid).thenApply(punish -> {
            if (punish != null && punish.getType() == PunishType.BAN && isPunishmentActive(punish)) {
                return Optional.of(punish);
            }
            return Optional.empty();
        });
    }

    public CompletableFuture<Boolean> isBanned(UUID playerUuid) {
        return getActiveBan(playerUuid).thenApply(Optional::isPresent);
    }

    public CompletableFuture<Boolean> isMuted(UUID playerUuid) {
        return getActiveMute(playerUuid).thenApply(Optional::isPresent);
    }

    public CompletableFuture<Long> countPunishments(UUID playerUuid, PunishmentTable reason) {
        return punishMongo.countAsync(
                Filters.and(
                        Filters.eq("userUuid", playerUuid),
                        Filters.eq("reason", reason.name())
                )
        );
    }

    public CompletableFuture<List<PunishEntry>> getAllPlayerHistory(UUID playerUuid) {
        return punishMongo.fetchAllAsyncFilter(Filters.eq("userUuid", playerUuid));
    }

    private boolean isPunishmentActive(PunishEntry entry) {
        if (entry == null) return false;
        boolean isExpired = entry.isActive();
        return !entry.isRevoked() && !isExpired;
    }

    private void cacheLocally(UUID playerUuid, PunishEntry entry) {
        punishedPlayers.put(playerUuid, entry);
        if (entry.getType() == PunishType.MUTE && isPunishmentActive(entry)) {
            mutedPlayers.put(playerUuid, entry);
        }
    }


    private String latestPunishKey(UUID userId) {
        return "punished:last:" + userId;
    }

    private String punishKey(UUID userId, UUID punishId) {
        return "punished:" + userId + ":" + punishId;
    }

    private String revokeKey(UUID userId, UUID revokeUuid) {
        return "revoke:" + userId + ":" + revokeUuid;
    }
}