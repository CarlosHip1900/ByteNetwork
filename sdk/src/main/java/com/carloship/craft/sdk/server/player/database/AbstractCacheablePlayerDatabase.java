package com.carloship.craft.sdk.server.player.database;

import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.database.cloud.RedisDatabase;
import com.carloship.craft.sdk.server.player.impl.AbstractPlayer;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class AbstractCacheablePlayerDatabase<E extends AbstractPlayer> {

    private static final String NAMED_INDEX = "indexes:name_index:";
    private static final Duration REDIS_TTL = Duration.ofMinutes(30);

    private final Cache<UUID, E> cache;

    private final RedisDatabase<E> redis;

    private final Class<E> pClass;

    private final String redisKey;

    public AbstractCacheablePlayerDatabase(Class<E> pClass, String redisKey, ModuleDependencyPacket packet) {

        this.pClass = pClass;
        this.redisKey = redisKey;

        this.redis = new RedisDatabase<>(packet.getRedis(), pClass, packet.getObjectMapper());

        this.cache = Caffeine.newBuilder().maximumSize(10_000).expireAfterAccess(Duration.ofMinutes(30)).build();
    }

    public void cacheEntity(E entity) {
        cache.put(entity.getUuid(), entity);
    }

    public E fetchCacheEntity(UUID uuid) {
        return cache.getIfPresent(uuid);
    }

    public E fetchCacheEntity(String username) {
        return cache.asMap().values().stream().filter(e -> e.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    public void uncacheEntity(UUID uuid) {
        cache.invalidate(uuid);
    }

    public CompletableFuture<E> fetchOrCreateEntity(UUID uuid, Supplier<E> creator) {

        return fetchEntityAsync(uuid).thenCompose(entity -> {

            if (entity != null) {
                return CompletableFuture.completedFuture(entity);
            }

            E newEntity = creator.get();

            cacheEntity(newEntity);

            return saveEntity(newEntity).thenApply(saved -> newEntity);
        });
    }

    public CompletableFuture<E> modify(E entity, Consumer<E> consumer) {

        return CompletableFuture.supplyAsync(() -> {

            consumer.accept(entity);

            cacheEntity(entity);

            saveEntity(entity);

            return entity;
        });
    }

    public CompletableFuture<Boolean> saveEntity(E entity) {

        redis.setWithOptionalTTLAsync(buildSecondKey(entity.getUsername()), entity.getUuid().toString(), null);

        return redis.saveAsync(redisKey(entity.getUuid()), entity, REDIS_TTL);
    }

    public CompletableFuture<E> fetchRedisEntity(UUID uuid) {
        return redis.fetchAsync(redisKey(uuid));
    }

    public CompletableFuture<E> fetchRedisEntity(String username) {

        String stringId = redis.getSyncCommands().get(buildSecondKey(username));

        if (stringId == null) {
            return CompletableFuture.completedFuture(null);
        }

        try {

            UUID uuid = UUID.fromString(stringId);

            return fetchRedisEntity(uuid);

        } catch (Exception e) {

            log.error("Error while fetching redis entity {}", pClass.getSimpleName(), e);

            return CompletableFuture.completedFuture(null);
        }
    }

    public CompletableFuture<E> fetchEntityAsync(UUID uuid) {

        E cached = fetchCacheEntity(uuid);

        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return fetchRedisEntity(uuid).thenApply(redisEntity -> {

            if (redisEntity != null) {
                cacheEntity(redisEntity);
            }

            return redisEntity;
        }).exceptionally(ex -> {

            log.error("Error while fetching entity {}", pClass.getSimpleName(), ex);

            return null;
        });
    }

    public CompletableFuture<E> fetchEntityAsync(String username) {

        E cached = fetchCacheEntity(username);

        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        return fetchRedisEntity(username).thenApply(redisEntity -> {

            if (redisEntity != null) {
                cacheEntity(redisEntity);
            }

            return redisEntity;
        }).exceptionally(ex -> {

            log.error("Error while fetching entity {}", pClass.getSimpleName(), ex);

            return null;
        });
    }

    public List<E> getInCacheEntities(){
        return cache.asMap().values().stream().toList();
    }

    public void shutdown() {
        cache.invalidateAll();
    }

    private String buildSecondKey(String name) {
        return NAMED_INDEX + name.toLowerCase();
    }

    public String redisKey(UUID uuid) {
        return redisKey + uuid;
    }
}