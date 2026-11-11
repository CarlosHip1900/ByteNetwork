package com.carloship.craft.sdk.server.player.impl;

import com.carloship.craft.sdk.module.ModuleDependencyPacket;
import com.carloship.craft.sdk.module.SDKModule;
import com.carloship.craft.sdk.server.player.database.AbstractPlayerDatabase;
import com.carloship.craft.sdk.server.player.pubsub.AbstractPlayerGenericPubSub;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public abstract class AbstractPlayerModule<E extends AbstractPlayer> implements SDKModule {

    private final AbstractPlayerGenericPubSub pubSub;

    @Getter
    private final List<Consumer<E>> refreshListeners = new CopyOnWriteArrayList<>();

    public AbstractPlayerModule(ModuleDependencyPacket packet) {
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

    public void markDirty(E entity) {
        getDatabase().markDirty(entity);
    }

    public void modify(E entity, Consumer<E> consumer) {
        consumer.accept(entity);

        markDirty(entity);
    }

    public void modifyAndSendFromProxy(E entity, Consumer<E> consumer){
        modify(entity, consumer);
        publishRefreshProxyToServer(entity.getUuid(), entity.getUsername());
    }

    public void modifyAndSendFromServer(E entity, Consumer<E> consumer){
        modify(entity, consumer);
        publishRefreshServerToProxy(entity.getUuid(), entity.getUsername());
    }

    public CompletableFuture<E> fetchOrCreateEntity(UUID uuid, Supplier<E> creator) {
        return getDatabase().fetchOrCreateEntity(uuid, creator);
    }

    public CompletableFuture<E> fetchOrCreateEntity(String username, Supplier<E> creator) {
        return getDatabase().fetchOrCreateEntity(username, creator);
    }

    public CompletableFuture<E> modifyAsync(E entity, Consumer<E> consumer) {
        return CompletableFuture.supplyAsync(() -> {
            consumer.accept(entity);
            markDirty(entity);
            return entity;
        });
    }

    public CompletableFuture<Long> modifyAndSendFromProxyAsync(E entity, Consumer<E> consumer) {
        return modifyAsync(entity, consumer)
                .thenCompose(modifiedEntity -> publishRefreshProxyToServerAsync(
                        modifiedEntity.getUuid(),
                        modifiedEntity.getUsername()
                ));
    }

    public CompletableFuture<Long> modifyAndSendFromServerAsync(E entity, Consumer<E> consumer) {
        return modifyAsync(entity, consumer)
                .thenCompose(modifiedEntity -> publishRefreshServerToProxyAsync(
                        modifiedEntity.getUuid(),
                        modifiedEntity.getUsername()
                ));
    }


    public CompletableFuture<E> refreshEntity(UUID uuid) {
        if (fetchCacheEntity(uuid) == null) {
            return CompletableFuture.completedFuture(null);
        }

        return fetchRedisEntity(uuid).thenApply(t -> {
            if (t != null) {
                cacheEntity(t);
            }
            return t;
        }).thenComposeAsync(e -> {
            if (e == null) {
                return CompletableFuture.completedFuture(null);
            }

            for (Consumer<E> listener : refreshListeners) {
                try {
                    listener.accept(e);
                } catch (Exception ex) {
                    log.error("Error executing refresh listener for entity {}", uuid, ex);
                }
            }
            return CompletableFuture.completedFuture(e);
        }).exceptionally(throwable -> {
            log.error("Error refresh entity {}", uuid, throwable);
            return null;
        });
    }


    public CompletableFuture<Boolean> saveRedisEntity(E entity) {
        return getDatabase().saveRedisEntity(entity);
    }

    public CompletableFuture<E> fetchRedisEntity(UUID uuid) {
        return getDatabase().fetchRedisEntity(uuid);
    }

    public CompletableFuture<E> fetchRedisEntity(String name) {
        return getDatabase().fetchRedisEntity(name);
    }

    public CompletableFuture<Boolean> saveMongoEntity(E entity) {
        return getDatabase().saveMongoEntity(entity);
    }

    public CompletableFuture<E> fetchMongoEntity(UUID uuid) {
        return getDatabase().fetchMongoEntity(uuid);
    }

    public CompletableFuture<E> fetchEntityAsync(UUID uuid) {
        return getDatabase().fetchEntityAsync(uuid);
    }

    public CompletableFuture<E> fetchEntityAsync(String name) {
        return getDatabase().fetchEntityAsync(name);
    }

    public E fetchEntitySync(UUID uuid) {
        return getDatabase().fetchEntitySync(uuid);
    }

    public E fetchEntityMongoSync(UUID uuid) {
        return getDatabase().fetchMongoEntitySync(uuid);
    }

    public E fetchEntityRedisSync(UUID uuid) {
        return getDatabase().fetchRedisEntitySync(uuid);
    }

    public CompletableFuture<Boolean> saveComplete(E entity) {
        return getDatabase().saveComplete(entity);
    }

    public List<E> getInCacheEntities() {
        return getDatabase().getInCacheEntities();
    }

    public void whenLeave(E entity) {
        if (entity == null) {
            return;
        }

        saveComplete(entity);
        uncacheEntity(entity.getUuid());
    }

    public void shutdownDatabase() {
        getDatabase().shutdown();
    }

    public void publishRefreshServerToProxy(UUID uuid, String target) {
        pubSub.publishRefreshServerToProxy(uuid, target);
    }

    public void publishRefreshProxyToServer(UUID uuid, String target) {
        pubSub.publishRefreshProxyToServer(uuid, target);
    }

    public CompletableFuture<Long> publishRefreshServerToProxyAsync(UUID uuid, String target) {
        return pubSub.publishRefreshServerToProxy(uuid, target);
    }

    public CompletableFuture<Long>  publishRefreshProxyToServerAsync(UUID uuid, String target) {
        return pubSub.publishRefreshProxyToServer(uuid, target);
    }

    protected void handlePubSub(UUID uuid, String target) {
        refreshEntity(uuid);
    }

    protected abstract String pubSubChannel();

    public abstract AbstractPlayerDatabase<E> getDatabase();

}