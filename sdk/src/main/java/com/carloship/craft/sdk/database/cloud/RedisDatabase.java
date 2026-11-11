package com.carloship.craft.sdk.database.cloud;

import com.carloship.craft.sdk.database.SDKAsyncDatabase;
import com.carloship.craft.sdk.database.SDKSyncDatabase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class RedisDatabase<T> implements SDKAsyncDatabase<T>, SDKSyncDatabase<T> {

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;

    @Getter
    private final RedisCommands<String, String> syncCommands;
    @Getter
    private final RedisAsyncCommands<String, String> asyncCommands;

    private final ObjectMapper mapper;
    private final Class<T> typeClass;

    public RedisDatabase(@NotNull String redisUrl, @NotNull Class<T> typeClass, @NotNull ObjectMapper mapper) {
        this(RedisClient.create(redisUrl), typeClass, mapper);
    }

    public RedisDatabase(@NotNull RedisClient redisClient, @NotNull Class<T> typeClass, @NotNull ObjectMapper mapper) {
        this.client = redisClient;
        this.connection = redisClient.connect();
        this.syncCommands = connection.sync();
        this.asyncCommands = connection.async();
        this.mapper = mapper;
        this.typeClass = typeClass;
    }

    @Override
    public T fetch(String key) {
        try {
            String json = syncCommands.get(key);
            return json == null ? null : mapper.readValue(json, typeClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch key: " + key, e);
        }
    }

    public T fetchHash(String key, String field) {
        try {

            String json = syncCommands.hget(key, field);

            if (json == null) {
                return null;
            }

            return mapper.readValue(json, typeClass);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch hash field: " + key + ":" + field, e);
        }
    }


    public CompletableFuture<T> fetchHashAsync(String key, String field) {

        return asyncCommands.hget(key, field)
                .thenApply(json -> {

                    if (json == null) {
                        return null;
                    }

                    try {
                        return mapper.readValue(json, typeClass);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }

                })
                .toCompletableFuture();
    }

    @Override
    public List<T> fetchAll() {
        throw new UnsupportedOperationException("Use fetchAll(prefix) or HSCAN to get all keys by pattern.");
    }

    public List<T> fetchAll(String prefix) {
        List<T> list = new ArrayList<>();
        try {
            for (String key : syncCommands.keys(prefix + "*")) {
                String json = syncCommands.get(key);
                if (json != null) list.add(mapper.readValue(json, typeClass));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch keys with prefix: " + prefix, e);
        }
        return list;
    }


    @Override
    public boolean save(String key, T value) {
        try {
            String json = mapper.writeValueAsString(value);
            syncCommands.set(key, json);
            return true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object for key: " + key, e);
        }
    }

    public boolean saveHash(String key, String field, T value) {
        try {

            String json = mapper.writeValueAsString(value);

            syncCommands.hset(key, field, json);

            return true;

        } catch (Exception e) {
            throw new RuntimeException("Failed to save hash field: " + key + ":" + field, e);
        }
    }

    public CompletableFuture<Boolean> saveHashAsync(String key, String field, T value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mapper.writeValueAsString(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).thenCompose(json ->
                asyncCommands.hset(key, field, json)
                        .thenApply(result -> result)
                        .toCompletableFuture()
        );
    }

    @Override
    public boolean save(String key, T value, Duration timeout) {
        try {
            String json = mapper.writeValueAsString(value);
            syncCommands.setex(key, timeout.toSeconds(), json);
            return true;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object for key: " + key, e);
        }
    }

    @Override
    public boolean delete(String key) {
        return syncCommands.del(key) > 0;
    }

    @Override
    public CompletableFuture<T> fetchAsync(String key) {
        return asyncCommands.get(key)
                .thenApply(json -> {
                    if (json == null) return null;
                    try {
                        return mapper.readValue(json, typeClass);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize object for key: " + key + ", JSON: " + json + ", Type: " + typeClass.getName(), e);
                    }
                })
                .toCompletableFuture();
    }

    @Override
    public CompletableFuture<List<T>> fetchAllAsync() {
        throw new UnsupportedOperationException("Use fetchAllAsync(prefix) for pattern-based fetching.");
    }

    public CompletableFuture<List<T>> fetchAllAsync(String prefix) {
        return asyncCommands.keys(prefix)
                .thenCompose(keys -> {
                    List<CompletableFuture<T>> futures = new ArrayList<>();
                    for (String key : keys) {
                        futures.add(fetchAsync(key));
                    }
                    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                            .thenApply(_2 -> futures.stream()
                                    .map(CompletableFuture::join)
                                    .toList());
                })
                .toCompletableFuture();
    }

    public List<T> fetchHashAll(String key) {

        try {

            List<T> list = new ArrayList<>();

            Map<String, String> entries = syncCommands.hgetall(key);

            for (String json : entries.values()) {
                list.add(mapper.readValue(json, typeClass));
            }

            return list;

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch hash values: " + key, e);
        }
    }

    @Override
    public CompletableFuture<Boolean> saveAsync(String key, T value) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).thenCompose(json ->
                asyncCommands.set(key, json)
                        .thenApply("OK"::equalsIgnoreCase)
                        .toCompletableFuture()
        ).exceptionally(ex -> {
            log.error("Error setting data in redis {} {}", getClass().getSimpleName(), ex.getMessage());
            return false;
        });
    }

    @Override
    public CompletableFuture<Boolean> saveAsync(String key, T value, Duration timeout) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return mapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).thenCompose(json ->
                asyncCommands.setex(key, timeout.toSeconds(), json)
                        .thenApply("OK"::equalsIgnoreCase)
                        .toCompletableFuture()
        ).exceptionally(ex -> {
            log.error("Error setting data with ttl in redis {} {}", getClass().getSimpleName(), ex.getMessage());
            return false;
        });
    }

    public boolean deleteHash(String key, String field) {
        return syncCommands.hdel(key, field) > 0;
    }

    public CompletableFuture<Boolean> deleteHashAsync(String key, String field) {
        return asyncCommands.hdel(key, field)
                .thenApply(result -> result > 0)
                .toCompletableFuture();
    }


    @Override
    public CompletableFuture<Boolean> deleteAsync(String key) {
        return asyncCommands.del(key)
                .thenApply(result -> result > 0)
                .toCompletableFuture();
    }

    public void setWithOptionalTTL(String key, String value, Duration ttl) {
        if (ttl == null) {
            getSyncCommands().set(key, value);
        } else {
            getSyncCommands().setex(key, ttl.getSeconds(), value);
        }
    }

    public CompletableFuture<Boolean> setWithOptionalTTLAsync(String key, String value, Duration ttl) {
        return (ttl == null
                ? getAsyncCommands().set(key, value)
                : getAsyncCommands().setex(key, ttl.getSeconds(), value)
        ).toCompletableFuture().thenApply(r -> true);
    }

    public void removeOptional(String key) {
        getSyncCommands().del(key);
    }

    public CompletableFuture<Long> removeOptionalAsync(String key) {
        return getAsyncCommands().del(key).toCompletableFuture();
    }

    public UUID safeParseUUID(String value) {
        try {
            return value == null ? null : UUID.fromString(value);
        } catch (Exception ignored) {
            return null;
        }
    }

    public void close() {
        connection.close();
        client.shutdown();
    }
}
