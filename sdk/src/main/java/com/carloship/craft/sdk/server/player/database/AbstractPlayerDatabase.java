package com.carloship.craft.sdk.server.player.database;

import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.database.cloud.MongoDatabase;
import com.carloship.craft.sdk.database.cloud.RedisDatabase;
import com.carloship.craft.sdk.server.player.impl.AbstractPlayer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.mongodb.client.model.Filters;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
public class AbstractPlayerDatabase<E extends AbstractPlayer> {

    private static final String NAMED_INDEX = "indexes:name_index:";
    private static final Duration REDIS_TTL = Duration.ofMinutes(30);

    private static final long SAVE_DELAY_MS = 30_000L;

    private static final ScheduledExecutorService SAVE_EXECUTOR = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r);
        thread.setName("player-save-worker");
        thread.setDaemon(true);
        return thread;
    });

    private final ConcurrentHashMap<UUID, Long> pendingSaves = new ConcurrentHashMap<>();

    private final Cache<UUID, E> cache;

    private final RedisDatabase<E> redis;
    private final MongoDatabase<E> mongo;

    private final Class<E> pClass;

    private final String redisKey;

    public AbstractPlayerDatabase(Class<E> pClass, String collection, String column, String redisKey, ModuleDependencyPacket packet) {
        this.pClass = pClass;
        this.redis = new RedisDatabase<>(packet.getRedis(), pClass, packet.getObjectMapper());
        this.mongo = new MongoDatabase<>(packet.getMongo(), collection, pClass, column);
        this.redisKey = redisKey;
        this.cache = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(Duration.ofMinutes(30))
                .removalListener((UUID uuid, E player, RemovalCause cause) -> {

                    if (player == null) {
                        return;
                    }

                    if (cause.wasEvicted()) {
                        flushPlayer(player);
                    }
                }).build();

        SAVE_EXECUTOR.scheduleAtFixedRate(this::flushPendingSaves, 5, 5, TimeUnit.SECONDS);
    }

    public void cacheEntity(E entity) {
        cache.put(entity.getUuid(), entity);
    }

    public E fetchCacheEntity(UUID uuid) {
        return cache.getIfPresent(uuid);
    }

    public E fetchCacheEntity(String username){
        return cache.asMap().values().stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    public CompletableFuture<E> fetchOrCreateEntity(UUID uuid, Supplier<E> creator) {
        return fetchEntityAsync(uuid).thenCompose(e -> generateEntity(e, creator));
    }

    public CompletableFuture<E> fetchOrCreateEntity(String username, Supplier<E> creator) {
        return fetchEntityAsync(username).thenCompose(e -> generateEntity(e, creator));
    }

    private CompletableFuture<E> generateEntity(E entity,  Supplier<E> creator){
        if (entity != null) {
            return CompletableFuture.completedFuture(entity);
        }

        E newEntity = creator.get();

        cacheEntity(newEntity);

        saveRedisEntity(newEntity);

        markDirty(newEntity);

        return CompletableFuture.completedFuture(newEntity);
    }

    public void uncacheEntity(UUID uuid) {
        cache.invalidate(uuid);
    }

    public void markDirty(E entity) {

        entity.markDirty();

        saveRedisEntity(entity);

        long nextSave = System.currentTimeMillis() + SAVE_DELAY_MS;

        pendingSaves.put(entity.getUuid(), nextSave);
    }
    private void flushPendingSaves() {

        long now = System.currentTimeMillis();

        for (Map.Entry<UUID, Long> entry : pendingSaves.entrySet()) {

            UUID uuid = entry.getKey();
            long saveTime = entry.getValue();

            if (now < saveTime) {
                continue;
            }

            E player = fetchCacheEntity(uuid);

            if (player == null) {
                pendingSaves.remove(uuid);
                continue;
            }

            flushPlayer(player);

            pendingSaves.remove(uuid);
        }
    }

    private void flushPlayer(E player) {

        if (!player.isDirty()) {
            return;
        }

        saveMongoEntity(player)
                .whenComplete((saved, throwable) -> {

                    if (throwable != null) {
                        log.error("Error while saving entity {}", pClass.getSimpleName(), throwable);
                        return;
                    }

                    if (!saved) {
                        log.warn("Cannot save entity {}", pClass.getSimpleName());
                        return;
                    }

                    player.clearDirty();

                    log.debug("Saved player {} ({})", player.getUsername(), player.getUuid());
                });
    }

    public CompletableFuture<Boolean> saveRedisEntity(E entity) {
        redis.setWithOptionalTTLAsync(buildSecondKey(entity.getUsername()), entity.getUuid().toString(), REDIS_TTL);

        return redis.saveAsync(redisKey(entity.getUuid()), entity, REDIS_TTL);
    }

    public CompletableFuture<E> fetchRedisEntity(String name) {

        var stringId = redis.getSyncCommands().get(buildSecondKey(name));

        if (stringId == null) {
            return CompletableFuture.completedFuture(null);
        }

        try {
            UUID uuid = UUID.fromString(stringId);

            return fetchRedisEntity(uuid);
        } catch (Exception e) {
            log.error("Error while fetch user id of model {} in redis sync", pClass.getSimpleName(), e);

            return CompletableFuture.completedFuture(null);
        }
    }

    public E fetchRedisEntitySync(String name){
        var stringId = redis.getSyncCommands().get(buildSecondKey(name));

        try {
            UUID uuid = UUID.fromString(stringId);

            return fetchRedisEntitySync(uuid);
        } catch (Exception e) {
            log.error("Error while fetch user id of model {} in redis sync", pClass.getSimpleName(), e);
            return null;
        }
    }

    public E fetchRedisEntitySync(UUID uuid){
        return redis.fetch(redisKey(uuid));
    }

    public CompletableFuture<E> fetchRedisEntity(UUID uuid) {
        return redis.fetchAsync(redisKey(uuid));
    }

    public CompletableFuture<Boolean> saveMongoEntity(E entity) {
        return mongo.saveAsync(entity);
    }

    public CompletableFuture<E> fetchMongoEntity(UUID uuid) {
        return mongo.fetchAsync(uuid.toString());
    }

    public CompletableFuture<E> fetchMongoEntity(String username){
        return mongo.fetchAsyncFilter(Filters.eq("username", username));
    }

    public E fetchMongoEntitySync(UUID uuid){
        return mongo.fetch(uuid.toString());
    }

    public E fetchEntitySync(UUID uuid){
        E cached = fetchCacheEntity(uuid);
        if (cached != null) return cached;

        E redisEntity = fetchRedisEntitySync(uuid);
        if (redisEntity != null) {
            return redisEntity;
        }

        return fetchMongoEntitySync(uuid);
    }

    public CompletableFuture<E> fetchEntityAsync(String username){
        E cached = fetchCacheEntity(username);
        if (cached != null){
            return CompletableFuture.completedFuture(cached);
        }

        return fetchRedisEntity(username)
                .thenCompose(redisEntity -> {
                    if (redisEntity != null){
                        cacheEntity(redisEntity);

                        return CompletableFuture.completedFuture(redisEntity);
                    }

                    return fetchMongoEntity(username)
                            .thenApply(mongoEntity -> {

                                if (mongoEntity != null){
                                    cacheEntity(mongoEntity);
                                    saveRedisEntity(mongoEntity);
                                }

                                return mongoEntity;
                            });
                })
                .exceptionally(ex -> {
                    log.error("Error while fetching entity {}", pClass.getSimpleName(), ex);
                    return null;
                });
    }

    public CompletableFuture<E> fetchEntityAsync(UUID uuid) {

        E cached = fetchCacheEntity(uuid);

        if (cached != null) return CompletableFuture.completedFuture(cached);

        return fetchRedisEntity(uuid)
                .thenCompose(redisEntity -> {

                    if (redisEntity != null) {
                        cacheEntity(redisEntity);

                        return CompletableFuture.completedFuture(redisEntity);
                    }

                    return fetchMongoEntity(uuid)
                            .thenApply(mongoEntity -> {

                                if (mongoEntity != null) {
                                    cacheEntity(mongoEntity);
                                    saveRedisEntity(mongoEntity);
                                }

                                return mongoEntity;
                            });
                })

                .exceptionally(ex -> {
                    log.error("Error while fetching entity {}", pClass.getSimpleName(), ex);
                    return null;
                });
    }


    public CompletableFuture<Boolean> saveComplete(E entity) {
        cacheEntity(entity);

        entity.markDirty();
        flushPlayer(entity);

        return CompletableFuture.completedFuture(true);
    }

    public List<E> getInCacheEntities(){
        return cache.asMap().values().stream().toList();
    }

    public void shutdown() {
        cache.asMap().values().forEach(this::flushPlayer);
        cache.invalidateAll();
        pendingSaves.clear();
    }

    private String buildSecondKey(String name) {
        return NAMED_INDEX + name.toLowerCase();
    }

    private void cleanPlayerName(E player) {
        redis.removeOptional(buildSecondKey(player.getUsername()));
    }

    public String redisKey(UUID uuid){
        return redisKey + uuid.toString();
    }
}