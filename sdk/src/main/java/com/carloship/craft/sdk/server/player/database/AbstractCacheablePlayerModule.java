package com.carloship.craft.sdk.server.player.database;

import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.server.player.impl.AbstractPlayer;
import com.carloship.craft.sdk.server.player.pubsub.AbstractPlayerGenericPubSub;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class AbstractCacheablePlayerModule<E extends AbstractPlayer> {

    private final AbstractPlayerGenericPubSub pubSub;

    public AbstractCacheablePlayerModule(ModuleDependencyPacket packet) {
        this.pubSub = new AbstractPlayerGenericPubSub(packet, pubSubChannel(), this::handlePubSub);
    }

    public void cacheEntity(E entity) {
        getDatabase().cacheEntity(entity);
    }

    public E fetchCacheEntity(UUID uuid) {
        return getDatabase().fetchCacheEntity(uuid);
    }

    public Optional<E> tryFetchCacheEntity(UUID uuid) {
        return Optional.ofNullable(fetchCacheEntity(uuid));
    }

    public void uncacheEntity(UUID uuid) {
        getDatabase().uncacheEntity(uuid);
    }

    public CompletableFuture<E> modify(E entity, Consumer<E> consumer) {

        return getDatabase().modify(entity, consumer);
    }

    public CompletableFuture<E> fetchOrCreateEntity(UUID uuid, Supplier<E> creator) {

        return getDatabase().fetchOrCreateEntity(uuid, creator);
    }

    public CompletableFuture<Boolean> saveEntity(E entity) {
        return getDatabase().saveEntity(entity);
    }

    public CompletableFuture<E> refreshEntity(UUID uuid) {

        return fetchRedisEntity(uuid).thenApply(entity -> {

            if (entity != null) {
                cacheEntity(entity);
            }

            return entity;
        });
    }

    public CompletableFuture<E> fetchRedisEntity(UUID uuid) {
        return getDatabase().fetchRedisEntity(uuid);
    }

    public CompletableFuture<E> fetchRedisEntity(String name) {
        return getDatabase().fetchRedisEntity(name);
    }

    public CompletableFuture<E> fetchEntityAsync(UUID uuid) {
        return getDatabase().fetchEntityAsync(uuid);
    }

    public CompletableFuture<E> fetchEntityAsync(String username) {
        return getDatabase().fetchEntityAsync(username);
    }

    public void whenLeave(E entity) {

        if (entity == null) {
            return;
        }

        saveEntity(entity);

        uncacheEntity(entity.getUuid());
    }

    public List<E> getInCacheEntities(){
        return getDatabase().getInCacheEntities();
    }

    public void publishRefreshServerToProxy(UUID uuid, String target) {

        pubSub.publishRefreshServerToProxy(uuid, target);
    }

    public void publishRefreshProxyToServer(UUID uuid, String target) {

        pubSub.publishRefreshProxyToServer(uuid, target);
    }

    protected void handlePubSub(UUID uuid, String target) {

        refreshEntity(uuid);
    }

    protected abstract String pubSubChannel();

    public abstract AbstractCacheablePlayerModule<E> getDatabase();
}